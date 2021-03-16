package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.FiasRecord;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

@Repository
@Transactional
public class FiasDaoImpl extends PgCrudDaoBase<FiasRecord, UUID> implements FiasDao {

    @Autowired
    public FiasDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<FiasRecord, UUID>builder("fias")
                .withFactory(FiasRecord::new)
                .withKeyColumn(
                    KeyColumnMapper.of(UUID.class, "aoguid",
                        FiasRecord::getAoguid, FiasRecord::setAoguid, x -> x))
                .withColumn(ColumnMapper.of(UUID.class, "aoid",
                    FiasRecord::getAoid, FiasRecord::setAoid))
                .withColumn(ColumnMapper.of(Integer.class, "aolevel",
                    FiasRecord::getAolevel, FiasRecord::setAolevel))
                .withColumn(ColumnMapper.of(Integer.class, "areacode",
                    FiasRecord::getAreacode, FiasRecord::setAreacode))
                .withColumn(ColumnMapper.of(Integer.class, "autocode",
                    FiasRecord::getAutocode, FiasRecord::setAutocode))
                .withColumn(ColumnMapper.of(Integer.class, "centstatus",
                    FiasRecord::getCentstatus, FiasRecord::setCentstatus))
                .withColumn(ColumnMapper.of(Integer.class, "citycode",
                    FiasRecord::getCitycode, FiasRecord::setCitycode))
                .withColumn(ColumnMapper.of(String.class, "code",
                    FiasRecord::getCode, FiasRecord::setCode))
                .withColumn(ColumnMapper.of(Integer.class, "currstatus",
                    FiasRecord::getCurrstatus, FiasRecord::setCurrstatus))
                .withColumn(ColumnMapper.of(LocalDate.class, "enddate",
                    FiasRecord::getEnddate, FiasRecord::setEnddate))
                .withColumn(ColumnMapper.of(String.class, "formalname",
                    FiasRecord::getFormalname, FiasRecord::setFormalname))
                .withColumn(ColumnMapper.of(String.class, "ifnsfl",
                    FiasRecord::getIfnsfl, FiasRecord::setIfnsfl))
                .withColumn(ColumnMapper.of(String.class, "ifnsul",
                    FiasRecord::getIfnsul, FiasRecord::setIfnsul))
                .withColumn(ColumnMapper.of(UUID.class, "nextid",
                    FiasRecord::getNextid, FiasRecord::setNextid))
                .withColumn(ColumnMapper.of(String.class, "offname",
                    FiasRecord::getOffname, FiasRecord::setOffname))
                .withColumn(ColumnMapper.of(String.class, "okato",
                    FiasRecord::getOkato, FiasRecord::setOkato))
                .withColumn(ColumnMapper.of(String.class, "oktmo",
                    FiasRecord::getOktmo, FiasRecord::setOktmo))
                .withColumn(ColumnMapper.of(Integer.class, "operstatus",
                    FiasRecord::getOperstatus, FiasRecord::setOperstatus))
                .withColumn(ColumnMapper.of(UUID.class, "parentguid",
                    FiasRecord::getParentguid, FiasRecord::setParentguid))
                .withColumn(ColumnMapper.of(Integer.class, "placecode",
                    FiasRecord::getPlacecode, FiasRecord::setPlacecode))
                .withColumn(ColumnMapper.of(String.class, "plaincode",
                    FiasRecord::getPlaincode, FiasRecord::setPlaincode))
                .withColumn(ColumnMapper.of(String.class, "postalcode",
                    FiasRecord::getPostalcode, FiasRecord::setPostalcode))
                .withColumn(ColumnMapper.of(UUID.class, "previd",
                    FiasRecord::getPrevid, FiasRecord::setPrevid))
                .withColumn(ColumnMapper.of(Integer.class, "regioncode",
                    FiasRecord::getRegioncode, FiasRecord::setRegioncode))
                .withColumn(ColumnMapper.of(String.class, "shortname",
                    FiasRecord::getShortname, FiasRecord::setShortname))
                .withColumn(ColumnMapper.of(LocalDate.class, "startdate",
                    FiasRecord::getStartdate, FiasRecord::setStartdate))
                .withColumn(ColumnMapper.of(Integer.class, "streetcode",
                    FiasRecord::getStreetcode, FiasRecord::setStreetcode))
                .withColumn(ColumnMapper.of(String.class, "terrifnsfl",
                    FiasRecord::getTerrifnsfl, FiasRecord::setTerrifnsfl))
                .withColumn(ColumnMapper.of(String.class, "terrifnsul",
                    FiasRecord::getTerrifnsul, FiasRecord::setTerrifnsul))
                .withColumn(ColumnMapper.of(LocalDate.class, "updatedate",
                    FiasRecord::getUpdatedate, FiasRecord::setUpdatedate))
                .withColumn(ColumnMapper.of(Integer.class, "ctarcode",
                    FiasRecord::getCtarcode, FiasRecord::setCtarcode))
                .withColumn(ColumnMapper.of(Integer.class, "extrcode",
                    FiasRecord::getExtrcode, FiasRecord::setExtrcode))
                .withColumn(ColumnMapper.of(Integer.class, "sextcode",
                    FiasRecord::getSextcode, FiasRecord::setSextcode))
                .withColumn(ColumnMapper.of(Integer.class, "livestatus",
                    FiasRecord::getLivestatus, FiasRecord::setLivestatus))
                .withColumn(ColumnMapper.of(UUID.class, "normdoc",
                    FiasRecord::getNormdoc, FiasRecord::setNormdoc))
                .withColumn(ColumnMapper.of(Integer.class, "plancode",
                    FiasRecord::getPlancode, FiasRecord::setPlancode))
                .withColumn(ColumnMapper.of(String.class, "cadnum",
                    FiasRecord::getCadnum, FiasRecord::setCadnum))
                .withColumn(ColumnMapper.of(Integer.class, "divtype",
                    FiasRecord::getDivtype, FiasRecord::setDivtype))

                .build(),
            jdbcTemplate);
    }
}
