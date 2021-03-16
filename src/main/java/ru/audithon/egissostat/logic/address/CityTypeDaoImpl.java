package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.CityType;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.function.Function;

@Repository
@Transactional
public class CityTypeDaoImpl extends PgCrudDaoBase<CityType, Integer> implements CityTypeDao {

    @Autowired
    public CityTypeDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<CityType, Integer>builder("addr_city_type")
            .withFactory(CityType::new)
            .withKeyColumn(
                KeyColumnMapper.of(Integer.class, "id",
                        CityType::getId, CityType::setId, Function.identity()))
            .withColumn(ColumnMapper.of(String.class, "caption",
                CityType::getCaption, CityType::setCaption))
            .withColumn(ColumnMapper.of(String.class, "short_caption",
                CityType::getShortCaption, CityType::setShortCaption))
            .build(),
            jdbcTemplate);
    }
}
