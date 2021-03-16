package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.FiasRecord;
import ru.audithon.egissostat.domain.common.LookupObject;
import ru.audithon.common.mapper.CrudDao;

import java.util.UUID;

public interface FiasDao extends CrudDao<FiasRecord, UUID> {
}
