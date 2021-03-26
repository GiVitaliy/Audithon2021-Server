package ru.audithon.egissostat.logic.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.common.helpers.ObjectUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;
import ru.audithon.egissostat.domain.common.Indicator;
import ru.audithon.egissostat.domain.common.IndicatorType;
import ru.audithon.egissostat.domain.common.LookupObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public class IndicatorDaoImpl extends PgCrudDaoBase<Indicator, Indicator.Key> implements IndicatorDao {

    @Autowired
    public IndicatorDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<Indicator, Indicator.Key>builder("indicator")
                .withFactory(Indicator::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "indicator_type_id",
                        Indicator::getIndicatorTypeId, Indicator::setIndicatorTypeId, Indicator.Key::getIndicatorTypeId))
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "state_id",
                        Indicator::getStateId, Indicator::setStateId, Indicator.Key::getStateId))
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "year",
                        Indicator::getYear, Indicator::setYear, Indicator.Key::getYear))
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "month",
                        Indicator::getMonth, Indicator::setMonth, Indicator.Key::getMonth))
                .withColumn(ColumnMapper.of(BigDecimal.class, "value",
                    Indicator::getValue, Indicator::setValue))
                .withColumn(ColumnMapper.of(BigDecimal.class, "value_ma",
                    Indicator::getValueMa, Indicator::setValueMa))
                .withColumn(ColumnMapper.of(BigDecimal.class, "value_ma_trend",
                    Indicator::getValueMaTrend, Indicator::setValueMaTrend))
                .withColumn(ColumnMapper.of(BigDecimal.class, "value_ma_trend_x2",
                    Indicator::getValueMaTrendX2, Indicator::setValueMaTrendX2))
                .build(),
            jdbcTemplate);
    }

    @Override
    public List<Indicator> getHistoryData(Integer indicatorTypeId, Integer stateId) {
        return jdbcTemplate.query(getSelectSql() + " where indicator_type_id = ? and state_id = ?",
            new Object[]{indicatorTypeId, stateId}, getMapper().getRowMapper());
    }

    @Override
    public List<Indicator> getPeriodData(Integer indicatorTypeId, Integer year, Integer month) {
        return jdbcTemplate.query(getSelectSql() + " where indicator_type_id = ? and year = ? and month = ?",
            new Object[]{indicatorTypeId, year, month}, getMapper().getRowMapper());
    }

    public void safelyStoreEgissoValues(LocalDate cDate, LookupObject state, List<Object> egissoValue,
                                        IndicatorType ind2, IndicatorType ind3, IndicatorType ind4,
                                        IndicatorType ind5, IndicatorType ind6, IndicatorType ind7,
                                        IndicatorType ind8, IndicatorType ind9, IndicatorType ind10, IndicatorType ind11) {
        safelyStoreEgissoVal(cDate, state.getId(), ind2, egissoValue.get(2));
        safelyStoreEgissoVal(cDate, state.getId(), ind3, egissoValue.get(3));
        safelyStoreEgissoVal(cDate, state.getId(), ind4, egissoValue.get(4));
        safelyStoreEgissoVal(cDate, state.getId(), ind5, egissoValue.get(5));
        safelyStoreEgissoVal(cDate, state.getId(), ind6, egissoValue.get(6));
        safelyStoreEgissoVal(cDate, state.getId(), ind7, egissoValue.get(7));
        safelyStoreEgissoVal(cDate, state.getId(), ind8, egissoValue.get(8));
        safelyStoreEgissoVal(cDate, state.getId(), ind9, egissoValue.get(9));
        safelyStoreEgissoVal(cDate, state.getId(), ind10, egissoValue.get(10));
        safelyStoreEgissoVal(cDate, state.getId(), ind11, egissoValue.get(11));
    }

    private void safelyStoreEgissoVal(LocalDate cDate, Integer stateId, IndicatorType indicatorType, Object rawValue) {

        BigDecimal value = null;
        if (rawValue != null) {
            value = new BigDecimal(rawValue.toString());
        }

        if (value != null) {
            Indicator existing = byId(new Indicator.Key(indicatorType.getId(), stateId, cDate.getYear(),
                cDate.getMonthValue())).orElse(null);

            if (existing == null) {
                insert(new Indicator(indicatorType.getId(), stateId, cDate.getYear(),
                    cDate.getMonthValue(), value, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
            } else {
                existing.setValue(value);
                update(existing, existing.getKey());
            }
        }
    }
}
