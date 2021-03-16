package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.FiasRRecord;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.UUID;

public interface FiasRDao extends CrudDao<FiasRRecord, UUID> {
    List<FiasRRecord> readBySignature(UUID houseguid, String signature);
}
