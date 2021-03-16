package ru.audithon.egissostat.logic.address;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.City;
import ru.audithon.egissostat.domain.address.CityType;
import ru.audithon.egissostat.domain.address.Region;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.*;
import java.util.function.Function;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Repository
@Transactional
public class RegionDaoImpl extends PgCrudDaoBase<Region, Integer> implements RegionDao {

    @Autowired
    public RegionDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<Region, Integer>builder("addr_region")
                .withFactory(Region::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "id",
                        Region::getId, Region::setId, Function.identity()))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    Region::getCaption, Region::setCaption))
                .withColumn(ColumnMapper.of(String.class, "pfr62_code",
                    Region::getCodePfr62, Region::setCodePfr62))
                .withColumn(ColumnMapper.of(UUID.class, "aoguid",
                    Region::getAoguid, Region::setAoguid))

                .build(),
            jdbcTemplate);
    }

    public Map<String, String> buildRegionsHash() {
        Map<String, String> retVal = new HashMap<>();

        all().forEach(region -> {

            List<String> codes = new ArrayList<>(Arrays.asList(isNull(region.getCodePfr62(), "").split("#")));
            codes.removeIf(Strings::isNullOrEmpty);
            String code = codes.size() > 0 ? codes.get(0) : region.getCaption();

            retVal.put(region.getId().toString(), code.toUpperCase());
        });

        return retVal;
    }
}
