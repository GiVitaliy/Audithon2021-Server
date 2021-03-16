package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.FiasHRecord;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class FiasHDaoImpl extends PgCrudDaoBase<FiasHRecord, UUID> implements FiasHDao {

    @Autowired
    public FiasHDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<FiasHRecord, UUID>builder("fias_h")
                .withFactory(FiasHRecord::new)
                .withKeyColumn(
                    KeyColumnMapper.of(UUID.class, "houseguid",
                        FiasHRecord::getHouseguid, FiasHRecord::setHouseguid, x -> x))

                .withColumn(ColumnMapper.of(UUID.class, "aoguid",
                    FiasHRecord::getAoguid, FiasHRecord::setAoguid))
                .withColumn(ColumnMapper.of(String.class, "buildnum",
                    FiasHRecord::getBuildnum, FiasHRecord::setBuildnum))
                .withColumn(ColumnMapper.of(LocalDate.class, "enddate",
                    FiasHRecord::getEnddate, FiasHRecord::setEnddate))
                .withColumn(ColumnMapper.of(Integer.class, "eststatus",
                    FiasHRecord::getEststatus, FiasHRecord::setEststatus))
                .withColumn(ColumnMapper.of(UUID.class, "houseid",
                    FiasHRecord::getHouseid, FiasHRecord::setHouseid))
                .withColumn(ColumnMapper.of(String.class, "housenum",
                    FiasHRecord::getHousenum, FiasHRecord::setHousenum))
                .withColumn(ColumnMapper.of(Integer.class, "statstatus",
                    FiasHRecord::getStatstatus, FiasHRecord::setStatstatus))
                .withColumn(ColumnMapper.of(String.class, "ifnsfl",
                    FiasHRecord::getIfnsfl, FiasHRecord::setIfnsfl))
                .withColumn(ColumnMapper.of(String.class, "ifnsul",
                    FiasHRecord::getIfnsul, FiasHRecord::setIfnsul))
                .withColumn(ColumnMapper.of(String.class, "okato",
                    FiasHRecord::getOkato, FiasHRecord::setOkato))
                .withColumn(ColumnMapper.of(String.class, "oktmo",
                    FiasHRecord::getOktmo, FiasHRecord::setOktmo))
                .withColumn(ColumnMapper.of(String.class, "postalcode",
                    FiasHRecord::getPostalcode, FiasHRecord::setPostalcode))
                .withColumn(ColumnMapper.of(LocalDate.class, "startdate",
                    FiasHRecord::getStartdate, FiasHRecord::setStartdate))
                .withColumn(ColumnMapper.of(String.class, "strucnum",
                    FiasHRecord::getStrucnum, FiasHRecord::setStrucnum))
                .withColumn(ColumnMapper.of(Integer.class, "strstatus",
                    FiasHRecord::getStrstatus, FiasHRecord::setStrstatus))
                .withColumn(ColumnMapper.of(String.class, "terrifnsfl",
                    FiasHRecord::getTerrifnsfl, FiasHRecord::setTerrifnsfl))
                .withColumn(ColumnMapper.of(String.class, "terrifnsul",
                    FiasHRecord::getTerrifnsul, FiasHRecord::setTerrifnsul))
                .withColumn(ColumnMapper.of(LocalDate.class, "updatedate",
                    FiasHRecord::getUpdatedate, FiasHRecord::setUpdatedate))
                .withColumn(ColumnMapper.of(UUID.class, "normdoc",
                    FiasHRecord::getNormdoc, FiasHRecord::setNormdoc))
                .withColumn(ColumnMapper.of(Integer.class, "counter",
                    FiasHRecord::getCounter, FiasHRecord::setCounter))
                .withColumn(ColumnMapper.of(String.class, "cadnum",
                    FiasHRecord::getCadnum, FiasHRecord::setCadnum))
                .withColumn(ColumnMapper.of(Integer.class, "divtype",
                    FiasHRecord::getDivtype, FiasHRecord::setDivtype))
                .withColumn(ColumnMapper.of(String.class, "signature",
                    FiasHRecord::getSignature, FiasHRecord::setSignature))
                .build(),
            jdbcTemplate);
    }

    public List<FiasHRecord> readBySignature(UUID aoguid, String signature) {
        return jdbcTemplate.query(getSelectSql() + " where aoguid = ? and signature = ?",
            new Object[]{aoguid, signature}, getMapper().getRowMapper());
    }
}
