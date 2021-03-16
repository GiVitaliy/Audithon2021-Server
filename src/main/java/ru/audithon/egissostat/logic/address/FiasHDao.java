package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.FiasHRecord;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.UUID;

public interface FiasHDao extends CrudDao<FiasHRecord, UUID> {
    List<FiasHRecord> readBySignature(UUID aoguid, String signature);
}
