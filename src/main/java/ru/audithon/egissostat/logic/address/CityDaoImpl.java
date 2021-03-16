package ru.audithon.egissostat.logic.address;

import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.infrastructure.AppCacheControl;
import ru.audithon.egissostat.domain.address.City;
import ru.audithon.egissostat.domain.common.LookupStrKeyObject;
import ru.audithon.common.helpers.PrefixTree;
import ru.audithon.common.helpers.StringUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.math.BigDecimal;
import java.util.*;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Repository
@Transactional
public class CityDaoImpl extends PgCrudDaoBase<City, City.CityKey> implements CityDao {

    @Autowired
    public CityDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<City, City.CityKey>builder("addr_city")
                .withFactory(City::new)
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "region_id",
                    City::getRegionId, City::setRegionId, City.CityKey::getRegionId))
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "id",
                    City::getId, City::setId, City.CityKey::getId))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    City::getCaption, City::setCaption))
                .withColumn(ColumnMapper.of(Integer.class, "type",
                    City::getType, City::setType))
                .withColumn(ColumnMapper.of(Boolean.class, "is_default",
                    City::getIsDefault, City::setIsDefault))
                .withColumn(ColumnMapper.of(BigDecimal.class, "rk",
                    City::getRk, City::setRk))
                .withColumn(ColumnMapper.of(String.class, "pfr62_code",
                    City::getCodePfr62, City::setCodePfr62))
                .withColumn(ColumnMapper.of(UUID.class, "aoguid",
                    City::getAoguid, City::setAoguid))
                .build(),
            jdbcTemplate);
    }

    public List<LookupStrKeyObject> readCityForLookup() {
        return jdbcTemplate.query("SELECT cast (c.region_id AS VARCHAR) || '-' || cast (c.id AS VARCHAR) AS id, " +
                "c.caption || coalesce(' ' || ct.short_caption, '') || coalesce(' (' || r.caption || ')', '') AS caption " +
                "FROM addr_city c " +
                "INNER JOIN addr_region r ON c.region_id = r.id " +
                "LEFT JOIN addr_city_type ct ON c.type = ct.id ORDER BY c.caption",
            new Object[]{},
            (rs, rowNum) -> new LookupStrKeyObject(rs.getString("id"), rs.getString("caption")));
    }

    public List<LookupStrKeyObject> readCityByRegionIdForLookup(int regionId) {
        return jdbcTemplate.query("SELECT cast (c.region_id AS VARCHAR) || '-' || cast (c.id AS VARCHAR) AS id, " +
                        "c.caption || coalesce(' ' || ct.short_caption, '') || coalesce(' (' || r.caption || ')', '') AS caption " +
                        "FROM addr_city c " +
                        "INNER JOIN addr_region r ON c.region_id = r.id " +
                        "LEFT JOIN addr_city_type ct ON c.type = ct.id " +
                        " WHERE c.region_id = ? " +
                        " ORDER BY c.caption",
                new Object[]{regionId},
                (rs, rowNum) -> new LookupStrKeyObject(rs.getString("id"), rs.getString("caption")));
    }

    public List<LookupStrKeyObject> readCityForLookupWithComments() {
        return jdbcTemplate.query("SELECT cast (c.region_id AS VARCHAR) || '-' || cast (c.id AS VARCHAR) AS id, " +
                "c.caption || coalesce(' ' || ct.short_caption, '') AS caption, r.caption AS comments " +
                "FROM addr_city c " +
                "INNER JOIN addr_region r ON c.region_id = r.id " +
                "LEFT JOIN addr_city_type ct ON c.type = ct.id ORDER BY c.caption",
            new Object[]{},
            (rs, rowNum) -> new LookupStrKeyObject(rs.getString("id"), rs.getString("caption"), rs.getString("comments")));
    }

    public City readByAoGuid(UUID aoguid) {
        List<City> retVal = jdbcTemplate.query(getSelectSql() + " where aoguid = ?",
            new Object[]{aoguid},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));
        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    public City readByCaption(int regionId, String caption) {
        List<City> retVal = jdbcTemplate.query(getSelectSql() + " where region_id = ? and lower(caption) = ?",
            new Object[]{regionId, caption.toLowerCase()},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));

        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    public City readByCaption(String caption) {
        List<City> retVal = jdbcTemplate.query(getSelectSql() + " where lower(caption) = ?",
            new Object[]{caption.toLowerCase()},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));

        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    public City findByShortCaption(String caption) {
        Objects.requireNonNull(caption);

        final String conditionalCaption = removeCaptionUnsignificantParts(caption);

        List<City> retVal = jdbcTemplate.query(getSelectSql()
                + " where lower(caption) = ? or replace(replace(lower(caption), '.', ' '), '  ', ' ') like ('%' || ? || '%')",
            new Object[]{conditionalCaption.toLowerCase(), conditionalCaption.toLowerCase()},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));

        //В приоритете полное совпадение искомой строки
        return retVal.size() > 0 ? retVal.stream()
            .filter(c -> c.getCaption().equalsIgnoreCase(conditionalCaption))
            .findFirst()
            .orElse(retVal.get(0))
            : null;
    }

    private String removeCaptionUnsignificantParts(String caption) {
        if (caption == null) {
            return "";
        }

        caption = caption.replace(".", " ");
        caption = caption.replace("  ", " ");
        caption = caption.replace("г ", "");
        caption = caption.replace(" г", "");
        caption = caption.replace("с ", "");
        caption = caption.replace(" с", "");
        caption = caption.replace("ст ", "");
        caption = caption.replace(" ст", "");
        caption = caption.replace("п ", "");
        caption = caption.replace(" п", "");
        caption = caption.replace("д ", "");
        caption = caption.replace(" д", "");
        caption = caption.replace("пгт ", "");
        caption = caption.replace(" пгт", "");
        caption = caption.trim();

        return caption;
    }

    public int getNextId(int regionId) {
        return jdbcTemplate.queryForObject("SELECT coalesce(max(id) + 1, 1) AS id FROM addr_city WHERE region_id = ?",
            new Object[]{regionId},
            (rs, rowNum) -> rs.getInt("id"));
    }

    @Cacheable(value = AppCacheControl.CACHE_ALL_CITIES, sync = true)
    public PrefixTree<City> getAllCitiesByCaptionMap() {

        ListMultimap<String, City> citiesMap = MultimapBuilder.hashKeys().arrayListValues().build();

        all().forEach(city -> citiesMap.put(StringUtils.normalizeAddress(isNull(city.getCaption(), "")), city));

        return PrefixTree.fromListMultiMap(citiesMap);
    }

    public Map<String, String> buildCitiesHash() {
        Map<String, String> retVal = new HashMap<>();

        all().forEach(city -> {

            List<String> codes = new ArrayList<>(Arrays.asList(isNull(city.getCodePfr62(), "").split("#")));
            codes.removeIf(Strings::isNullOrEmpty);
            String code = codes.size() > 0 ? codes.get(0) : city.getCaption();

            retVal.put(city.getRegionId().toString() + ":" + city.getId().toString(), code.toUpperCase());
        });

        return retVal;
    }
}
