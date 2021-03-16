package ru.audithon.common.mapper;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class TableMapper<TTable, TKey> {
    private static final Logger logger = LoggerFactory.getLogger(TableMapper.class);

    public static final ResultSetMethodMapper rsMapper = new ResultSetMethodMapper();

    @Getter private final String tableSchema;
    @Getter private final String tableName;
    @Getter private final ImmutableList<ColumnMapper<TTable, ?>> columnMappers;
    @Getter protected final HashMap<String, ColumnMapper<TTable, ?>> mappersHash;
    @Getter private final ImmutableList<KeyColumnMapper<TTable, TKey, ?>> keyColumnMappers;
    @Getter private final VersionColumnMapper<TTable, ?> versionColumnMapper;
    @Getter private final Supplier<TTable> factory;

    @Getter private final DynamicRowMapper<TTable> rowMapper;

    @Getter private final String qualifiedTableName;

    private TableMapper(String tableName, String tableSchema,
                        Supplier<TTable> factory,
                        List<ColumnMapper<TTable, ?>> columnMappers,
                        List<KeyColumnMapper<TTable, TKey, ?>>  keyColumnMappers,
                        VersionColumnMapper<TTable, ?> versionColumnMapper)
    {
        // -- checking arguments
        Objects.requireNonNull(tableName, "tableName is null");
        Objects.requireNonNull(factory, "factory is null");
        Objects.requireNonNull(columnMappers, "columnMappers is null");
        Objects.requireNonNull(keyColumnMappers, "keyColumnMappers is null");

        this.tableName = tableName.trim();
        if (Strings.isNullOrEmpty(this.tableName)) {
            throw new IllegalArgumentException("tableName is empty");
        }

        Set<String> uniqueNameTest = columnMappers.stream()
            .map(ColumnMapper::getColumnName).collect(Collectors.toSet());
        if (uniqueNameTest.size() != columnMappers.size()) {
            throw new MapperException("column names are not unique");
        }
        // -----

        this.tableSchema = tableSchema == null ? null : Strings.emptyToNull(tableSchema.trim());
        this.factory = factory;

        this.columnMappers = ImmutableList.copyOf(columnMappers);
        this.keyColumnMappers = ImmutableList.copyOf(keyColumnMappers);
        this.versionColumnMapper = versionColumnMapper;
        this.mappersHash = buildMappersHash(this.columnMappers);

        this.rowMapper = new DynamicRowMapper<TTable>(factory, this.mappersHash);

        this.qualifiedTableName = (tableSchema == null  ? "" : tableSchema + ".") + tableName;
    }

    public static <T> HashMap<String, ColumnMapper<T, ?>> buildMappersHash(List<ColumnMapper<T, ?>> columnMappers                                                                           ) {
        HashMap<String, ColumnMapper<T, ?>> mappersHash = new HashMap<>();

        for (int i = 0; i < columnMappers.size(); ++i) {
            ColumnMapper cm = columnMappers.get(i);
            mappersHash.put(cm.getColumnName().toUpperCase(), cm);
        }

        return mappersHash;
    }

    public static <TTable, TKey> TableMapperBuilder<TTable, TKey> builder(String tableSchema, String tableName) {
        return new TableMapperBuilder<>(tableSchema, tableName);
    }

    public static <TTable, TKey> TableMapperBuilder<TTable, TKey> builder(String tableName) {
        return new TableMapperBuilder<>(tableName);
    }

    public void readResultSetRow(ResultSet rs, TTable value) throws SQLException {
        readResultSetRow(rs, value, this.mappersHash);
    }

    @SuppressWarnings("unchecked")
    public static <T> void readResultSetRow(ResultSet rs, T value,
                                            HashMap<String, ColumnMapper<T, ?>> mappersHash) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {

            if (!mappersHash.containsKey(rsmd.getColumnName(i).toUpperCase())) {
                continue;
            }

            ColumnMapper cm = mappersHash.get(rsmd.getColumnName(i).toUpperCase());

            SqlBiFunction<ResultSet, Integer, ?> function = rsMapper.mapper.get(cm.getFieldClass());
            if (function != null) {
                Object fieldValue = function.apply(rs, i);
                cm.getSetter().accept(value, fieldValue);
            }
        }
    }

    public static class DynamicRowMapper<T> implements RowMapper<T> {
        private final Supplier<T> factory;
        private final HashMap<String, ColumnMapper<T, ?>> mappersHash;

        private DynamicRowMapper(Supplier<T> factory, HashMap<String, ColumnMapper<T, ?>> mappersHash) {
            this.factory = factory;
            this.mappersHash = mappersHash;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T value = factory.get();
            readResultSetRow(rs, value, mappersHash);

            return value;
        }
    }

    public static class TableMapperBuilder<TTable, TKey> {
        private String tableSchema;
        private String tableName;
        private Supplier<TTable> factory;
        private List<ColumnMapper<TTable, ?>> columnMappers = new ArrayList<>();
        private List<KeyColumnMapper<TTable, TKey, ?>> keyColumnMappers = new ArrayList<>();
        private VersionColumnMapper<TTable, ?> versionColumnMapper;

        private TableMapperBuilder(String tableName) {
            this.tableName = tableName;
        }

        private TableMapperBuilder(String tableSchema, String tableName) {
            this.tableSchema = tableSchema;
            this.tableName = tableName;
        }

        public TableMapperBuilder<TTable, TKey> withFactory(Supplier<TTable> factory) {
            this.factory = factory;
            return this;
        }

        public TableMapperBuilder<TTable, TKey> withColumn(ColumnMapper<TTable, ?> columnMapper)
        {
            columnMappers.add(columnMapper);
            return this;
        }

        public TableMapperBuilder<TTable, TKey> withKeyColumn(KeyColumnMapper<TTable, TKey, ?> columnMapper)
        {
            columnMappers.add(columnMapper);
            keyColumnMappers.add(columnMapper);

            return this;
        }

        public TableMapperBuilder<TTable, TKey> withVersionColumn(VersionColumnMapper<TTable, ?> columnMapper)
        {
            if (versionColumnMapper != null ) {
                throw new MapperException("Only one versionColumnMapper allowed");
            }

            columnMappers.add(columnMapper);
            versionColumnMapper = columnMapper;

            return this;
        }

        public TableMapper<TTable, TKey> build() {
            return new TableMapper<>(tableName, tableSchema, factory, columnMappers, keyColumnMappers, versionColumnMapper);
        }
    }
}
