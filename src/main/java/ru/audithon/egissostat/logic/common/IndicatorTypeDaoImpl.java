package ru.audithon.egissostat.logic.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;
import ru.audithon.egissostat.domain.common.IndicatorType;

import java.util.List;
import java.util.function.Function;

@Repository
public class IndicatorTypeDaoImpl extends PgCrudDaoBase<IndicatorType, Integer> implements IndicatorTypeDao {

    @Autowired
    public IndicatorTypeDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<IndicatorType, Integer>builder("indicator_type")
                .withFactory(IndicatorType::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "id",
                        IndicatorType::getId, IndicatorType::setId, Function.identity(), true))
                .withColumn(ColumnMapper.of(String.class, "code",
                    IndicatorType::getCode, IndicatorType::setCode))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    IndicatorType::getCaption, IndicatorType::setCaption))
                .withColumn(ColumnMapper.of(String.class, "description",
                    IndicatorType::getDescription, IndicatorType::setDescription))
                .withColumn(ColumnMapper.of(Boolean.class, "favorite",
                    IndicatorType::getFavorite, IndicatorType::setFavorite))
                .withColumn(ColumnMapper.of(Boolean.class, "negative",
                    IndicatorType::getNegative, IndicatorType::setNegative))
                .withColumn(ColumnMapper.of(String.class, "group_caption",
                    IndicatorType::getGroup, IndicatorType::setGroup))
                .build(),
            jdbcTemplate);
    }

    @Override
    public IndicatorType byCode(String indicatorCode) {
        List<IndicatorType> retVal = jdbcTemplate.query(getSelectSql() + " where code = ?",
            new Object[]{indicatorCode}, getMapper().getRowMapper());
        if (retVal.size() == 1) {
            return retVal.get(0);
        } else {
            return null;
        }
    }
}
