package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.CityType;
import ru.audithon.egissostat.domain.address.StreetType;
import ru.audithon.common.helpers.StringUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.Optional;
import java.util.function.Function;

@Repository
@Transactional
public class StreetTypeDaoImpl extends PgCrudDaoBase<StreetType, Integer> implements StreetTypeDao {

    @Autowired
    public StreetTypeDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<StreetType, Integer>builder("addr_street_type")
                .withFactory(StreetType::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "id",
                        StreetType::getId, StreetType::setId, Function.identity()))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    StreetType::getCaption, StreetType::setCaption))
                .withColumn(ColumnMapper.of(String.class, "short_caption",
                    StreetType::getShortCaption, StreetType::setShortCaption))
                .build(),
            jdbcTemplate);
    }

    public Optional<StreetType> byCode(String code) {

        code = StringUtils.prettify(code);

        if (code == null) {
            return Optional.empty();
        }

        try {
            StreetType val = jdbcTemplate.queryForObject(getSelectSql() + " where short_caption = ?",
                new Object[]{code}, getMapper().getRowMapper());
            return Optional.of(val);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
