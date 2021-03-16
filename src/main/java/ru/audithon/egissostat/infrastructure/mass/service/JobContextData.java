package ru.audithon.egissostat.infrastructure.mass.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.common.telemetry.TelemetryServiceCore;

@Data
@AllArgsConstructor
public class JobContextData {
    private JobKey jobKey;
    TelemetryServiceCore.TelemetryOperationToken telemetryToken;
}
