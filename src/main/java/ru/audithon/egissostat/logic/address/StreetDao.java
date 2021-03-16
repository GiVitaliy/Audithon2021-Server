package ru.audithon.egissostat.logic.address;

import com.google.common.collect.ListMultimap;
import ru.audithon.egissostat.domain.address.Street;
import ru.audithon.egissostat.domain.common.LookupStrKeyObject;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StreetDao extends CrudDao<Street, Street.StreetKey> {
    List<LookupStrKeyObject> readStreetForLookup(Integer regionId, Integer cityId);
    Street readByAoGuid(UUID aoguid);
    Street readByCaption(int regionId, int cityId, String caption);
    Street findByShortCaption(int regionId, int cityId, String caption);
    List<Street> readByCity(int regionId, int cityId);
    int getNextId(int regionId, int cityId);
    ListMultimap<String, Street> getStreetsByCityByCaptionMap(Integer regionId, Integer cityId);
    Map<String, String> buildStreetsHash();
}
