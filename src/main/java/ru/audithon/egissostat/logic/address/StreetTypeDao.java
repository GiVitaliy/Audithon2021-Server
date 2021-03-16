package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.CityType;
import ru.audithon.egissostat.domain.address.StreetType;
import ru.audithon.common.mapper.CrudDao;

import java.util.Optional;

public interface StreetTypeDao extends CrudDao<StreetType, Integer> {
    Optional<StreetType> byCode(String code);
}
