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
import ru.audithon.egissostat.domain.address.Street;
import ru.audithon.egissostat.domain.common.LookupStrKeyObject;
import ru.audithon.common.helpers.StringUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.*;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Repository
@Transactional
public class StreetDaoImpl extends PgCrudDaoBase<Street, Street.StreetKey> implements StreetDao {

    @Autowired
    public StreetDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<Street, Street.StreetKey>builder("addr_street")
                .withFactory(Street::new)
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "region_id",
                    Street::getRegionId, Street::setRegionId, Street.StreetKey::getRegionId))
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "city_id",
                    Street::getCityId, Street::setCityId, Street.StreetKey::getCityId))
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "id",
                    Street::getId, Street::setId, Street.StreetKey::getId))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    Street::getCaption, Street::setCaption))
                .withColumn(ColumnMapper.of(Integer.class, "type",
                    Street::getType, Street::setType))
                .withColumn(ColumnMapper.of(String.class, "pfr62_code",
                    Street::getCodePfr62, Street::setCodePfr62))
                .withColumn(ColumnMapper.of(UUID.class, "aoguid",
                    Street::getAoguid, Street::setAoguid))
                .build(),
            jdbcTemplate);
    }

    public List<LookupStrKeyObject> readStreetForLookup(Integer regionId, Integer cityId) {
        return jdbcTemplate.query("SELECT s.id, s.caption || coalesce(' ' || st.short_caption, '') AS caption " +
                "FROM addr_street s LEFT JOIN addr_street_type st ON s.type = st.id " +
                "WHERE s.region_id = ? AND s.city_id = ? ORDER BY s.caption",
            new Object[]{regionId, cityId},
            (rs, rowNum) -> new LookupStrKeyObject(rs.getString("id"), rs.getString("caption")));
    }

    public Street readByAoGuid(UUID aoguid) {
        List<Street> retVal = jdbcTemplate.query(getSelectSql() + " where aoguid = ?",
            new Object[]{aoguid},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));
        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    public Street readByCaption(int regionId, int cityId, String caption) {
        List<Street> retVal = jdbcTemplate.query(getSelectSql() + " where region_id = ? and city_id = ? and lower(caption) = ?",
            new Object[]{regionId, cityId, caption.toLowerCase()},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));
        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    public List<Street> readByCity(int regionId, int cityId) {
        return jdbcTemplate.query(getSelectSql() + " where region_id = ? and city_id = ?",
            new Object[]{regionId, cityId},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));
    }

    public Street findByShortCaption(int regionId, int cityId, String caption) {
        Objects.requireNonNull(caption);
        final String conditionalCaption = removeUnsignificantParts(caption);

        List<Street> retVal = jdbcTemplate.query(getSelectSql() + " where region_id = ? and city_id = ? and lower(caption) = ?",
            new Object[]{regionId, cityId, conditionalCaption.toLowerCase()},
            (rs, rowNum) -> getMapper().getRowMapper().mapRow(rs, rowNum));
        return retVal.size() > 0 ? retVal.get(0) : null;
    }

    private String removeUnsignificantParts(String caption) {
        if (caption == null) {
            return "";
        }

        caption = caption.replace(".", " ");
        caption = caption.replace("  ", " ");
        caption = caption.replace("ул ", "");
        caption = caption.replace(" ул", "");
        caption = caption.replace("пр ", "");
        caption = caption.replace(" пр", "");
        caption = caption.replace("пер ", "");
        caption = caption.replace(" пер", "");
        caption = caption.replace("пл ", "");
        caption = caption.replace(" пл", "");
        caption = caption.replace("мкр ", "");
        caption = caption.replace(" мкр", "");
        caption = caption.replace("аллея ", "");
        caption = caption.replace(" аллея", "");
        caption = caption.replace("б-р ", "");
        caption = caption.replace(" б-р", "");
        caption = caption.replace("бульвар ", "");
        caption = caption.replace(" бульвар", "");
        caption = caption.trim();

        return caption;
    }

    @Cacheable(value = AppCacheControl.CACHE_STREETS_BY_CITY, key = "{ #regionId, #cityId }")
    public ListMultimap<String, Street> getStreetsByCityByCaptionMap(Integer regionId, Integer cityId) {

        ListMultimap<String, Street> streetMap = MultimapBuilder.hashKeys().arrayListValues().build();

        readByCity(regionId, cityId).forEach(street -> {
            String normalized = StringUtils.normalizeAddress(isNull(street.getCaption(), ""));
            streetMap.put(normalized, street);
            String[] normalizedParts = normalized.split(" ");
            for (int i = 1; i < normalizedParts.length; i++) {
                streetMap.put("$weak:" + buildStringNameTrimmed(normalizedParts, i), street);
            }
        });

        return streetMap;
    }

    private String buildStringNameTrimmed(String[] streetParts, int trimEndParts) {
        StringBuilder retVal = new StringBuilder();
        for (int i = 0; i < streetParts.length - trimEndParts; i++) {
            retVal.append(streetParts[i]);
            retVal.append(" ");
        }
        return retVal.toString().trim();
    }

    public int getNextId(int regionId, int cityId) {
        return jdbcTemplate.queryForObject("SELECT coalesce(max(id) + 1, 1) AS id FROM addr_street WHERE region_id = ? AND city_id = ?",
            new Object[]{regionId, cityId},
            (rs, rowNum) -> rs.getInt("id"));
    }

    public Map<String, String> buildStreetsHash() {
        Map<String, String> retVal = new HashMap<>();

        all().forEach(street -> {

            List<String> codes = new ArrayList<>(Arrays.asList(isNull(street.getCodePfr62(), "").split("#")));
            codes.removeIf(Strings::isNullOrEmpty);
            String code = codes.size() > 0 ? codes.get(0) : street.getCaption();

            retVal.put(street.getRegionId().toString() + ":" + street.getCityId().toString() + ":" + street.getId().toString(), code.toUpperCase());
        });

        return retVal;
    }
}
