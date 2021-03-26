package ru.audithon.egissostat.logic.common;

import ru.audithon.common.mapper.CrudDao;
import ru.audithon.egissostat.domain.common.IndicatorType;

public interface IndicatorTypeDao extends CrudDao<IndicatorType, Integer> {
    IndicatorType byCode(String indicatorCode);
}
