package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.Region;
import ru.audithon.common.mapper.CrudDao;

import java.util.Map;

public interface RegionDao extends CrudDao<Region, Integer> {
    Map<String, String> buildRegionsHash();
}
