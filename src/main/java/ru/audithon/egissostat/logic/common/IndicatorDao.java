package ru.audithon.egissostat.logic.common;

import ru.audithon.common.mapper.CrudDao;
import ru.audithon.egissostat.domain.common.Indicator;
import ru.audithon.egissostat.domain.common.IndicatorType;
import ru.audithon.egissostat.domain.common.LookupObject;

import java.time.LocalDate;
import java.util.List;

public interface IndicatorDao extends CrudDao<Indicator, Indicator.Key> {
    void safelyStoreEgissoValues(LocalDate cDate, LookupObject state, List<Object> egissoValue,
                                 IndicatorType ind2, IndicatorType ind3, IndicatorType ind4,
                                 IndicatorType ind5, IndicatorType ind6, IndicatorType ind7,
                                 IndicatorType ind8, IndicatorType ind9, IndicatorType ind10, IndicatorType ind11);

    List<Indicator> getHistoryData(Integer indicatorTypeId, Integer stateId);

    List<Indicator> getPeriodData(Integer indicatorTypeId, Integer year, Integer month);
}
