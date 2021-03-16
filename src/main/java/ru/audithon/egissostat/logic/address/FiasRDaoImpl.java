package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.address.FiasRRecord;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class FiasRDaoImpl extends PgCrudDaoBase<FiasRRecord, UUID> implements FiasRDao {

    @Autowired
    public FiasRDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<FiasRRecord, UUID>builder("fias_r")
                .withFactory(FiasRRecord::new)
                .withKeyColumn(
                    KeyColumnMapper.of(UUID.class, "roomguid",
                        FiasRRecord::getRoomguid, FiasRRecord::setRoomguid, x -> x))

                .withColumn(ColumnMapper.of(UUID.class, "houseguid",
                    FiasRRecord::getHouseguid, FiasRRecord::setHouseguid))
                .withColumn(ColumnMapper.of(UUID.class, "roomid",
                    FiasRRecord::getRoomid, FiasRRecord::setRoomid))
                .withColumn(ColumnMapper.of(String.class, "regioncode",
                    FiasRRecord::getRegioncode, FiasRRecord::setRegioncode))
                .withColumn(ColumnMapper.of(String.class, "flatnumber",
                    FiasRRecord::getFlatnumber, FiasRRecord::setFlatnumber))
                .withColumn(ColumnMapper.of(Integer.class, "flattype",
                    FiasRRecord::getFlattype, FiasRRecord::setFlattype))
                .withColumn(ColumnMapper.of(String.class, "roomnumber",
                    FiasRRecord::getRoomnumber, FiasRRecord::setRoomnumber))
                .withColumn(ColumnMapper.of(Integer.class, "roomtype",
                    FiasRRecord::getRoomtype, FiasRRecord::setRoomtype))
                .withColumn(ColumnMapper.of(String.class, "cadnum",
                    FiasRRecord::getCadnum, FiasRRecord::setCadnum))
                .withColumn(ColumnMapper.of(String.class, "roomcadnum",
                    FiasRRecord::getRoomcadnum, FiasRRecord::setRoomcadnum))
                .withColumn(ColumnMapper.of(String.class, "postalcode",
                    FiasRRecord::getPostalcode, FiasRRecord::setPostalcode))
                .withColumn(ColumnMapper.of(LocalDate.class, "updatedate",
                    FiasRRecord::getUpdatedate, FiasRRecord::setUpdatedate))
                .withColumn(ColumnMapper.of(UUID.class, "previd",
                    FiasRRecord::getPrevid, FiasRRecord::setPrevid))
                .withColumn(ColumnMapper.of(UUID.class, "nextid",
                    FiasRRecord::getNextid, FiasRRecord::setNextid))
                .withColumn(ColumnMapper.of(Integer.class, "operstatus",
                    FiasRRecord::getOperstatus, FiasRRecord::setOperstatus))
                .withColumn(ColumnMapper.of(LocalDate.class, "startdate",
                    FiasRRecord::getStartdate, FiasRRecord::setStartdate))
                .withColumn(ColumnMapper.of(LocalDate.class, "enddate",
                    FiasRRecord::getEnddate, FiasRRecord::setEnddate))
                .withColumn(ColumnMapper.of(Integer.class, "livestatus",
                    FiasRRecord::getLivestatus, FiasRRecord::setLivestatus))
                .withColumn(ColumnMapper.of(UUID.class, "normdoc",
                    FiasRRecord::getNormdoc, FiasRRecord::setNormdoc))
                .withColumn(ColumnMapper.of(String.class, "signature",
                    FiasRRecord::getSignature, FiasRRecord::setSignature))
                .build(),
            jdbcTemplate);
    }

    public List<FiasRRecord> readBySignature(UUID houseguid, String signature) {
        return jdbcTemplate.query(getSelectSql() + " where houseguid = ? and signature = ?",
            new Object[]{houseguid, signature}, getMapper().getRowMapper());
    }
}
