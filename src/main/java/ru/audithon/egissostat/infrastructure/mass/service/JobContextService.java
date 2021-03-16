package ru.audithon.egissostat.infrastructure.mass.service;

import org.springframework.stereotype.Service;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.jobs.JobType;
import ru.audithon.common.telemetry.TelemetryServiceCore;

@Service
public class JobContextService {

    private final TelemetryServiceCore telemetryServiceCore;

    public JobContextService(TelemetryServiceCore telemetryServiceCore) {
        this.telemetryServiceCore = telemetryServiceCore;
    }

    public void initContextAtJobStart(JobKey jobKey) {
        JobContextData contextData = JobContextManager.readContext();

        JobType jt = JobType.getConstantDictionaryContentMap2().get(jobKey.getTypeId());

        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation(
            "@Job::" + jt.getRunnerBeanName() + " [" + jt.getId() + "]");
        if (contextData == null) {
            JobContextManager.writeContext(new JobContextData(jobKey, optoken));
        } else {
            contextData.setJobKey(jobKey);
            contextData.setTelemetryToken(optoken);
        }

    }

    public JobKey getCurrentJobId() {
        JobContextData contextData = JobContextManager.readContext();
        if (contextData != null) {
            return contextData.getJobKey();
        }

        return null;
    }

    public void finalizeContextAtJobEnd() {

        JobContextData contextData = JobContextManager.readContext();

        telemetryServiceCore.exitOperation(contextData.getTelemetryToken());

        JobContextManager.writeContext(new JobContextData(null, null));
    }
}
