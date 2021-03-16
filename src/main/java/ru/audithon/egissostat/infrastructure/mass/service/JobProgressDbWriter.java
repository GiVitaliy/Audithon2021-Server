package ru.audithon.egissostat.infrastructure.mass.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.infrastructure.mass.config.JobRunnerConfiguration;
import ru.audithon.egissostat.infrastructure.mass.dao.JobDao;
import ru.audithon.egissostat.infrastructure.mass.domain.Job;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;
import ru.audithon.egissostat.infrastructure.mass.domain.JobState;
import ru.audithon.egissostat.infrastructure.mass.helpers.JobParametersHelper;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.common.telemetry.TelemetryServiceCore;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@EnableAsync
public class JobProgressDbWriter {
    private static final Logger logger = LoggerFactory.getLogger(JobProgressDbWriter.class);

    private final JobDao jobDao;
    private final JobProgressMonitor progressMonitor;
    private final JobRunnerConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final TelemetryServiceCore telemetryServiceCore;

    @Autowired
    public JobProgressDbWriter(JobDao jobDao, JobProgressMonitor progressMonitor,
                               JobRunnerConfiguration configuration,
                               ObjectMapper objectMapper,
                               TelemetryServiceCore telemetryServiceCore) {
        this.jobDao = jobDao;
        this.progressMonitor = progressMonitor;
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.telemetryServiceCore = telemetryServiceCore;
    }

    @Scheduled(fixedDelayString = "${job-runner.db-writer-interval-ms}")
    public void save() {

        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::JobProgressDbWriter::save");
        try {
            Map<JobKey, JobProgress> progress = progressMonitor.getProgress();
            if (!progress.isEmpty()) {
                logger.debug("Saving job progress...");

                progress.forEach((key, p) -> {
                    int cnt = jobDao.updateProgress(p);
                    logger.debug("{} records written", cnt);
                });
            }
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    public Job createJob(int type, JobParameters parameters) {
        try {
            Job job = Job.builder()
                .typeId(type)
                .created(LocalDateTime.now())
                .progress(0)
                .nodeId(configuration.getNodeId())
                .stateId(JobState.NEW)
                .parameters(objectMapper.writeValueAsString(parameters))
                .digest(JobParametersHelper.buildParamsDigest(parameters))
                .build();

            return jobDao.insert(job);

        } catch (JsonProcessingException e) {
            throw new JobException("Error serializing job parameters: " + parameters, e);
        }
    }
}
