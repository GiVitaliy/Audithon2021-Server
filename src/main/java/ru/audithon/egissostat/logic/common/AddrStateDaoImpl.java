package ru.audithon.egissostat.logic.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;
import ru.audithon.egissostat.domain.common.LookupObject;

import java.util.function.Function;

@Repository
public class AddrStateDaoImpl extends PgCrudDaoBase<LookupObject, Integer> implements AddrStateDao {

    @Autowired
    public AddrStateDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<LookupObject, Integer>builder("addr_state")
                .withFactory(LookupObject::new)
                .withKeyColumn(
                    KeyColumnMapper.of(Integer.class, "id",
                        LookupObject::getId, LookupObject::setId, Function.identity()))
                .withColumn(ColumnMapper.of(String.class, "caption",
                    LookupObject::getCaption, LookupObject::setCaption))
                .build(),
            jdbcTemplate);
    }
}
