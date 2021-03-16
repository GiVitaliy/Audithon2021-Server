package ru.audithon.common.mapper;

import com.google.common.base.Strings;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.Assert;
import ru.audithon.common.exceptions.BusinessLogicException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PgCrudDaoBase<TTable, TKey> implements PgCrudDao<TTable, TKey> {
    private static final Logger logger = LoggerFactory.getLogger(PgCrudDaoBase.class);

    private ThreadLocal<HashMap<TKey, TTable>> _stashedObjects = new ThreadLocal<>();

    @Getter protected final TableMapper<TTable, TKey> mapper;
    @Getter protected final HashMap<String, Integer> explicitColumnTypes;
    @Getter protected final JdbcTemplate jdbcTemplate;

    @Getter private final String selectSql;
    @Getter private final String selectByIdSql;
    @Getter private final String insertSql;
    @Getter private final int[] insertSqlArgsExplicitTypes;
    @Getter private final String updateSql;
    @Getter private final int[] updateSqlArgsExplicitTypes;
    @Getter private final String updateWithVersionSql;
    @Getter private final int[] updateWithVersionSqlArgsExplicitTypes;
    @Getter private final String deleteSql;
    @Getter private final int[] deleteSqlArgsExplicitTypes;

    @Getter private final String sequenceName;

    public PgCrudDaoBase(TableMapper<TTable, TKey> mapper, JdbcTemplate jdbcTemplate) {
        this(mapper, jdbcTemplate, null, null);
    }

    public PgCrudDaoBase(TableMapper<TTable, TKey> mapper, JdbcTemplate jdbcTemplate, String sequenceName){
        this(mapper, jdbcTemplate, sequenceName, null);
    }

    public PgCrudDaoBase(TableMapper<TTable, TKey> mapper, JdbcTemplate jdbcTemplate, String sequenceName,
                         HashMap<String, Integer> explicitColumnTypes) {
        Objects.requireNonNull(mapper, "mapper is null");
        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate is null");

        this.mapper = mapper;
        this.explicitColumnTypes = explicitColumnTypes;
        this.jdbcTemplate = jdbcTemplate;
        this.sequenceName = sequenceName;

        // SQL шаблоны
        this.selectSql = "select "
            + mapper.getColumnMappers().stream()
            .map(ColumnMapper::getColumnName)
            .collect(Collectors.joining(", "))
            + " from " + mapper.getQualifiedTableName();

        this.selectByIdSql = selectSql + " where "
            + mapper.getKeyColumnMappers().stream()
            .map(s -> s.getColumnName() + " = ?")
            .collect(Collectors.joining(" and "));

        String generatedColumns = mapper.getColumnMappers().stream()
            .filter(ColumnMapper::isGenerated)
            .map(ColumnMapper::getColumnName)
            .collect(Collectors.joining(", "));

        if (logger.isDebugEnabled() && !generatedColumns.isEmpty()) {
            logger.debug("{}: generated columns: {}", mapper.getQualifiedTableName(), generatedColumns);
        }

        List<String> insertableColumns = mapper.getColumnMappers().stream()
            .filter(ColumnMapper::isInsertable)
            .map(ColumnMapper::getColumnName)
            .collect(Collectors.toList());

        this.insertSql = "insert into "
            + mapper.getQualifiedTableName() + " ("
            + insertableColumns.stream().collect(Collectors.joining(", "))
            + ") values ("
            +  getPlaceholderString(insertableColumns.size())
            + ")"
            + (Strings.isNullOrEmpty(generatedColumns) ? "" : " returning " + generatedColumns);
        this.insertSqlArgsExplicitTypes = getFieldsExplicitSqlTypes(insertableColumns);

        List<String> updateFieldsArgs = new ArrayList<>();
        this.updateSql = "update "
            + mapper.getQualifiedTableName() + " set "
            + mapper.getColumnMappers().stream()
            .filter(ColumnMapper::isUpdatable)
            .map(cm -> { updateFieldsArgs.add(cm.getColumnName());  return cm.getColumnName()  + " = ?"; })
            .collect(Collectors.joining(", "))
            + " where "
            + mapper.getKeyColumnMappers().stream()
            .map(cm -> { updateFieldsArgs.add(cm.getColumnName()); return cm.getColumnName() + " = ?"; })
            .collect(Collectors.joining(" and "));
        this.updateSqlArgsExplicitTypes = getFieldsExplicitSqlTypes(updateFieldsArgs);

        Integer versionColumnExplicitType = mapper.getVersionColumnMapper() != null ?
                getFieldExplicitSqlTypes(mapper.getVersionColumnMapper().getColumnName()) : null;

        this.updateWithVersionSql = mapper.getVersionColumnMapper() == null ? ""
            : this.updateSql + " and " + mapper.getVersionColumnMapper().getColumnName() + " = ?";

        if(updateSqlArgsExplicitTypes != null && versionColumnExplicitType != null) {
            this.updateWithVersionSqlArgsExplicitTypes = new int[updateSqlArgsExplicitTypes.length + 1];
            System.arraycopy(updateSqlArgsExplicitTypes, 0, updateWithVersionSqlArgsExplicitTypes, 0,
                    updateSqlArgsExplicitTypes.length);
            this.updateWithVersionSqlArgsExplicitTypes[this.updateWithVersionSqlArgsExplicitTypes.length - 1]
                    = versionColumnExplicitType;
        } else {
            updateWithVersionSqlArgsExplicitTypes = null;
        }

        this.deleteSql = "delete from "
            + mapper.getQualifiedTableName()
            + " where "
            + mapper.getKeyColumnMappers().stream()
                .map(cm -> cm.getColumnName() + " = ?")
                .collect(Collectors.joining(" and "));
        this.deleteSqlArgsExplicitTypes = getFieldsExplicitSqlTypes(mapper.getKeyColumnMappers().stream()
                .map(ColumnMapper::getColumnName).collect(Collectors.toList()));
    }

    public TTable cloneRecord(TTable record) {
        TTable clone = mapper.getFactory().get();
        mapper.getColumnMappers().forEach(cm -> {
            cm.copyFields(record, clone);

        });

        return clone;
    }

    public int nextSequenceValue() {
        return nextSequenceValue(sequenceName);
    }

    public int nextSequenceValue(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalStateException("sequence name is not set");
        }

        return jdbcTemplate.queryForObject("select nextval(?)", new Object[] {name}, Integer.class);
    }

    public TTable newRecord() {
        return mapper.getFactory().get();
    }

    @Override
    public List<TTable> all() {
        logger.debug(selectSql);

        return jdbcTemplate.query(selectSql, mapper.getRowMapper());
    }

    @Override
    public <THKey> HashMap<THKey, TTable> getAllHash(Function<TTable, THKey> getKey) {
        HashMap<THKey, TTable> retVal = new HashMap<>();
        all().forEach(item -> {
            retVal.put(getKey.apply(item), item);
        });
        return retVal;
    }

    @Override
    public Optional<TTable> byId(TKey key) {
        return byId(key, false);
    }

    @Override
    public TTable byIdStrong(TKey key) {
        return byId(key, false).orElseThrow(() -> new BusinessLogicException(null, "a18main.Common.objectNotFound"));
    }

    @Override
    public TTable byIdStashed(TKey key) {
        if (_stashedObjects.get() != null) {
            TTable val = _stashedObjects.get().get(key);
            if (val != null) {
                return val;
            }
        }

        TTable val = byIdStrong(key);

        if (_stashedObjects.get() == null) {
            _stashedObjects.set(new HashMap<>());
        }

        _stashedObjects.get().put(key, val);

        return val;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<TTable> byId(TKey key, boolean updLock) {
        Objects.requireNonNull(key, "key is null");

        logger.debug(selectByIdSql);

        Object[] keyValues = mapper.getKeyColumnMappers().stream().map(cm -> cm.getKeyGetter().apply(key)).toArray();
        if (Arrays.stream(keyValues).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("key object should not contains null values");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("values: " + Arrays.stream(keyValues).map(Object::toString)
                .collect(Collectors.joining(", ")));
        }

        try {
            String sql = selectByIdSql;

            if (updLock) {
                sql = sql + " for update";
            }

            TTable value = jdbcTemplate.queryForObject(
                sql,
                keyValues,
                mapper.getRowMapper());

            return Optional.of(value);

        } catch (EmptyResultDataAccessException ignored) {}

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TTable> byIds(Collection<TKey> keys) {
        Objects.requireNonNull(keys, "keys is null");

        if (keys.size() == 0) {
            return new ArrayList<>();
        }

        StringBuilder sqlBuilder = new StringBuilder(getSelectSql());
        sqlBuilder.append(" where ");

        int keyFieldsCount = mapper.getKeyColumnMappers().size();
        Object[] parameters = new Object[keys.size() * keyFieldsCount];

        int keyIndex = 0;
        for (TKey key : keys) {
            List<Object> keyValues = mapper.getKeyColumnMappers().stream().map(cm -> cm.getKeyGetter().apply(key))
                    .collect(Collectors.toList());
            if (keyValues.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("key object should not contains null values");
            }

            if (keyIndex > 0) {
                sqlBuilder.append(" or ");
            }

            sqlBuilder.append("(");

            int fieldIndex = 0;
            for (Object keyValue : keyValues) {
                parameters[keyIndex * keyFieldsCount + fieldIndex] = keyValue;

                if (fieldIndex > 0) {
                    sqlBuilder.append(" and ");
                }
                sqlBuilder.append(mapper.getKeyColumnMappers().get(fieldIndex).getColumnName()).append(" = ?");
                fieldIndex++;
            }

            sqlBuilder.append(")");

            keyIndex++;
        }

        String sql = sqlBuilder.toString();

        if (logger.isDebugEnabled()) {
            logger.debug(sql + ": " + Arrays.stream(parameters).map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }

        return jdbcTemplate.query(
                sql,
                parameters,
                mapper.getRowMapper());
    }

    private void notInsertableNotGeneratedToNull(TTable value) {
        mapper.getColumnMappers().stream()
            .filter(cm -> !cm.isInsertable() && !cm.isGenerated())
            .forEach(cm -> {
                cm.getSetter().accept(value, null);
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public TTable insert(TTable value) {
        Objects.requireNonNull(value, "value is null");

        logger.debug(insertSql);

        if (mapper.getVersionColumnMapper() != null) {
            mapper.getVersionColumnMapper().setNextVersion(value);
        }

        Object[] fieldValues = mapper.getColumnMappers().stream()
            .filter(ColumnMapper::isInsertable)
            .map(cm -> cm.getGetter().apply(value)).toArray();

        List<ColumnMapper<TTable, ?>> generated = mapper.getColumnMappers().stream()
            .filter(ColumnMapper::isGenerated)
            .collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            logger.debug("values: " + Arrays.stream(fieldValues).map(v -> v == null ? "null" : v.toString())
                .collect(Collectors.joining(", ")));
        }

        if (generated.size() == 0) {
            int cnt = update(insertSql, fieldValues, insertSqlArgsExplicitTypes);
            return cnt > 0 ? cloneRecord(value) : null;
        }

        HashMap<String, ColumnMapper<TTable, ?>> generatedHash = TableMapper.buildMappersHash(generated);

        return query(insertSql, fieldValues, insertSqlArgsExplicitTypes, rs -> {
            TTable result = cloneRecord(value);
            if (rs.next()) {
                TableMapper.readResultSetRow(rs, result, generatedHash);
                notInsertableNotGeneratedToNull(result);
                return result;
            }
            return null;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public int update(TTable value, TKey where) {
        Objects.requireNonNull(value, "value is null");
        Objects.requireNonNull(where, "where is null");

        Object version = null;
        if (mapper.getVersionColumnMapper() != null) {
            version = mapper.getVersionColumnMapper().setNextVersion(value);
        }

        try {

            // заполняем значения для update-а
            List<Object> values = mapper.getColumnMappers().stream()
                    .filter(ColumnMapper::isUpdatable)
                    .map(cm -> cm.getGetter().apply(value))
                    .collect(Collectors.toList());

            // заполняем значения для where
            List<Object> keyValues = new ArrayList<>();
            mapper.getKeyColumnMappers().forEach(km -> {
                keyValues.add(km.getKeyGetter().apply(where));
            });

            // если у нас есть предыдущий таймстэмп, добавляем его в условие
            if (version != null) {
                keyValues.add(version);
            }

        String currentUpdateSql = version != null ? updateWithVersionSql : updateSql;
        logger.debug(currentUpdateSql);
        int[] currentUpdateArgsExplicitTypes = version != null ? updateWithVersionSqlArgsExplicitTypes
                : updateSqlArgsExplicitTypes;

            values.addAll(keyValues);

            if (logger.isDebugEnabled()) {
                logger.debug("values: " + values.stream().map(v -> v == null ? "null" : v.toString())
                        .collect(Collectors.joining(", ")));
            }

            int updateResult = update(currentUpdateSql, values.toArray(), currentUpdateArgsExplicitTypes);
            if (version == null) {
                return updateResult;
            } else {
                if (updateResult == 0) {
                    throw new OptimisticLockingFailureException("value is changed");
                } else {
                    return updateResult;
                }
            }
        } catch(Exception ex) {
            if (mapper.getVersionColumnMapper() != null) {
                mapper.getVersionColumnMapper().setOldVersion(value, version);
            }
            throw ex;
        }
    }

    @Override
    public int delete(TKey key) {
        Objects.requireNonNull(key, "key is null");
        logger.debug(deleteSql);

        Object[] keyValues = mapper.getKeyColumnMappers().stream().map(cm -> cm.getKeyGetter().apply(key)).toArray();
        if (Arrays.stream(keyValues).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("key object should not contains null values");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("values: " + Arrays.stream(keyValues).map(Object::toString)
                .collect(Collectors.joining(", ")));
        }

        return update(deleteSql, keyValues, deleteSqlArgsExplicitTypes);
    }

    @Override
    public List<TTable> syncCollection(List<TTable> items, List<TTable> existing, Function<TTable, TKey> keyGetter,
                                       BiPredicate<TTable, TTable> itemChangeDetector) {
        Objects.requireNonNull(items, "items is null");
        Objects.requireNonNull(keyGetter, "keyGetter is null");
        Objects.requireNonNull(existing, "existing is null");

        List<TKey> keysToDelete = new ArrayList<>();
        List<TTable> itemsToUpdate = new ArrayList<>();
        List<TTable> itemsToInsert = new ArrayList<>();

        Map<TKey, TTable> existingByKeys = existing.stream().collect(Collectors.toMap(keyGetter, Function.identity()));
        Set<TKey> actualKeys = new HashSet<>();

        Set<Field> keyFields = null;

        for (TTable item : items) {
            TKey key = keyGetter.apply(item);
            actualKeys.add(key);

            if (key == null) {
                itemsToInsert.add(item);
                continue;
            }

            if (keyFields == null) {
                keyFields = Arrays.stream(key.getClass().getDeclaredFields()).collect(Collectors.toSet());
            }

            boolean isNewItem = false;

            for (Field keyField : keyFields) {
                if (!keyField.isAccessible()) {
                    keyField.setAccessible(true);
                }

                try {
                    if (keyField.get(key) == null) {
                        isNewItem = true;
                        break;
                    }
                } catch (IllegalAccessException e) {
                    //nothing to do
                }
            }

            if (isNewItem) {
                itemsToInsert.add(item);
                continue;
            }

            if (!existingByKeys.containsKey(key)) {
                itemsToInsert.add(item);
            } else if (itemChangeDetector != null && itemChangeDetector.test(item, existingByKeys.get(key))) {
                itemsToUpdate.add(item);
            }
        }

        for (TKey existingKey: existingByKeys.keySet()) {
            if (!actualKeys.contains(existingKey)) {
                keysToDelete.add(existingKey);
            }
        }

        keysToDelete.forEach(this::delete);

        List<TTable> results = new ArrayList<>();

        // формируем результирующий список в порядке элементов входного
        items.forEach(item -> {
            TTable touchedItem = null;
            if (itemsToUpdate.contains(item)) {
                update(item, keyGetter.apply(item));
                touchedItem = item;
            }

            if (itemsToInsert.contains(item)) {
                touchedItem = insert(item);
            }

            if (touchedItem != null) {
                results.add(touchedItem);
            } else {
                results.add(item);
            }
        });

        return results;
    }

    protected static String getPlaceholderString(int count) {
        Assert.isTrue(count > 0, "count must be greater then zero");

        String[] placeHolders = new String[count];
        Arrays.fill(placeHolders, "?");

        return Arrays.stream(placeHolders).collect(Collectors.joining(", "));
    }

    protected Integer getFieldExplicitSqlTypes(String fieldName) {
        if (getExplicitColumnTypes() == null || getExplicitColumnTypes().size() == 0)
            return null;

        return getFieldsExplicitSqlTypes(fieldName)[0];
    }

    protected int[] getFieldsExplicitSqlTypes(List<String> fieldNames) {
        return getFieldsExplicitSqlTypes(fieldNames.toArray(new String[0]));
    }

    protected int[] getFieldsExplicitSqlTypes(String... fieldNames){
        if (getExplicitColumnTypes() == null || getExplicitColumnTypes().size() == 0)
            return new int[0];

        int[] resultArray = new int[fieldNames.length];
        int i = 0;
        for (String fieldName : fieldNames) {
            Integer fieldType = getExplicitColumnTypes().getOrDefault(fieldName, null);

            if(fieldType == null) {
                fieldType = tryCalculateFieldSqlType(fieldName);
            }

            if (fieldType == null) {
                throw new BusinessLogicException(null, "Класс %s не содержит описания БД-типа поля %s, " +
                        "и не удалось определить тип автоматически в методе tryCalculateFieldSqlType",
                        this.getClass(), fieldName);
            }

            resultArray[i++] = fieldType;
        }


        return resultArray;
    }

    private Integer tryCalculateFieldSqlType(String fieldName) {
        if (getMapper() == null) return null;
        if (getMapper().getMappersHash() == null) return null;

        String fieldMapKey = fieldName.toUpperCase();

        if (!getMapper().getMappersHash().containsKey(fieldMapKey)) return null;

        ColumnMapper columnMapper = getMapper().getMappersHash().get(fieldMapKey);

        if (columnMapper.getFieldClass() == Integer.class) return Types.INTEGER;
        if (columnMapper.getFieldClass() == Long.class) return Types.BIGINT;
        if (columnMapper.getFieldClass() == LocalDate.class) return Types.DATE;
        if (columnMapper.getFieldClass() == LocalDateTime.class) return Types.TIMESTAMP;
        if (columnMapper.getFieldClass() == String.class) return Types.VARCHAR;
        if (columnMapper.getFieldClass() == BigDecimal.class) return Types.DECIMAL;
        if (columnMapper.getFieldClass() == BigInteger.class) return Types.DECIMAL;
        if (columnMapper.getFieldClass() == Boolean.class) return Types.BOOLEAN;

        return null;
    }

    protected <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
        return query(sql, args, null, rse);
    }

    protected <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse)
            throws DataAccessException {
        if (argTypes != null && argTypes.length > 0) {
            return jdbcTemplate.query(sql, args, argTypes, rse);
        } else {
            return jdbcTemplate.query(sql, args, rse);
        }
    }

    protected int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
        if (argTypes != null && argTypes.length > 0) {
            return jdbcTemplate.update(sql, args, argTypes);
        } else {
            return jdbcTemplate.update(sql, args);
        }
    }

    protected int update(String sql, Object... args) throws DataAccessException {
        return update(sql, args, null);
    }

    @Override
    public void cleanupStash() {
        _stashedObjects.set(null);
    }
}
