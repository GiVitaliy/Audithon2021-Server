package ru.audithon.egissostat.logic.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;
import ru.audithon.egissostat.domain.common.IndicatorType;

import java.util.function.Function;

@Repository
public class IndicatorTypeDaoImpl extends PgCrudDaoBase<IndicatorType, Integer> implements IndicatorTypeDao {

    @Autowired
    public IndicatorTypeDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<IndicatorType, Integer>builder("indicator_type")
                .withFactory(IndicatorType::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "id",
                        IndicatorType::getId, IndicatorType::setId, Function.identity()))
                .withColumn(ColumnMapper.of(String.class, "code",
                    IndicatorType::getCode, IndicatorType::setCode))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    IndicatorType::getCaption, IndicatorType::setCaption))
                .withColumn(ColumnMapper.of(String.class, "description",
                    IndicatorType::getDescription, IndicatorType::setDescription))
                .build(),
            jdbcTemplate);
    }
}
