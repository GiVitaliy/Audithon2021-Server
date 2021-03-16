package ru.audithon.egissostat.logic.address;

import com.google.common.collect.ListMultimap;
import ru.audithon.egissostat.domain.address.City;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.egissostat.domain.common.LookupStrKeyObject;
import ru.audithon.common.helpers.PrefixTree;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface CityDao extends CrudDao<City, City.CityKey> {
    List<LookupStrKeyObject> readCityForLookup();
    List<LookupStrKeyObject> readCityByRegionIdForLookup(int regionId);
    List<LookupStrKeyObject> readCityForLookupWithComments();
    City readByAoGuid(UUID aoguid);
    City readByCaption(int regionId, String caption);
    City readByCaption(String caption);
    City findByShortCaption(String caption);
    int getNextId(int regionId);
    PrefixTree<City> getAllCitiesByCaptionMap();
    Map<String, String> buildCitiesHash();
}
