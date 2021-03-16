package ru.audithon.egissostat.infrastructure.mass.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;
import ru.audithon.egissostat.infrastructure.ExceptionTranslator;
import ru.audithon.egissostat.infrastructure.mass.config.JobRunnerConfiguration;
import ru.audithon.egissostat.infrastructure.mass.dao.JobDao;
import ru.audithon.egissostat.infrastructure.mass.domain.*;
import ru.audithon.egissostat.jobs.JobParameters;
import ru.audithon.egissostat.jobs.JobType;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.telemetry.TelemetryServiceCore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.audithon.common.helpers.ObjectUtils.isNull;

@Service
@Transactional
public class JobExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutionService.class);

    private final ApplicationContext context;
    private final JobDao jobDao;
    private final JobProgressMonitorControl monitorService;
    private final JobRunnerConfiguration configuration;
    private final JobProgressDbWriter jobProgressDbWriter;
    private final ExceptionTranslator exceptionTranslator;
    private final TelemetryServiceCore telemetryServiceCore;

    private final ConcurrentMap<JobKey, ListenableFutureState<JobProgress>> jobs = new ConcurrentHashMap<>();

    @Autowired
    public JobExecutionService(ApplicationContext context,
                               JobDao jobDao,
                               JobProgressMonitorControl monitorService,
                               JobProgressDbWriter jobProgressDbWriter,
                               JobRunnerConfiguration configuration,
                               ExceptionTranslator exceptionTranslator,
                               TelemetryServiceCore telemetryServiceCore) {
        this.context = context;
        this.jobDao = jobDao;
        this.monitorService = monitorService;
        this.configuration = configuration;
        this.jobProgressDbWriter = jobProgressDbWriter;
        this.exceptionTranslator = exceptionTranslator;
        this.telemetryServiceCore = telemetryServiceCore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void clearRunningJob() {
        jobDao.changeStates(configuration.getNodeId(), JobState.RUNNING, JobState.TERMINATED,
                "Terminated on service start");
    }

    public JobKey startJob(JobParameters parameters) {

        JobType jobType = safeGetJobType(parameters);

        return startJobUnsecured(parameters);
    }

    public JobKey startJobUnsecured(JobParameters parameters) {

        JobType jobType = safeGetJobType(parameters);

        JobRunner runner = createRunner(parameters.getType());

        Job job = jobProgressDbWriter.createJob(jobType.getId(), parameters);

        startJob(runner, job.getKey(), parameters);

        return job.getKey();
    }

    private JobType safeGetJobType(JobParameters parameters) {
        JobType jobType = JobType.getConstantDictionaryContentMap().get(parameters.getType());

        if (jobType == null) {
            throw new BusinessLogicException("Неизвестный вид регламентной операции: '" + parameters.getType() + "'", null);
        }
        return jobType;
    }

    private JobRunner createRunner(String jobTypeCode) {

        JobRunner runner = context.getBean(jobTypeCode, JobRunner.class);

        if (runner == null) {
            throw new JobException("Unknown job type");
        }

        return runner;
    }

    public Optional<JobProgress> getJobProgress(JobKey key) {

        JobType jobType = JobType.getConstantDictionaryContentMap2().get(key.getTypeId());

        Optional<JobProgress> progress = monitorService.getProgress(key);
        if (isNull(jobType.getIsVolatile(), false) && progress.isPresent()
            && progress.get().getState() == JobState.COMPLETED) {
            removeJob(key);
            jobDao.delete(key);
        }

        return progress;
    }

    @Scheduled(fixedDelay=10000)
    public void evictUnrequestedVolatiles() {

        // результаты isVolatile=true операций у нас не пишутся в БД, и сами операции удаляются из памяти и из БД после того,
        // как клиент запросил результаты и мы их отдали. Если же клиент результаты не запрашивает, мы их удаляем вот в
        //этой операции по прошествии 30 секунд
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::JobExecutionService::evictUnrequestedVolatiles");
        try {
            Map<JobKey, JobProgress> progress = monitorService.getProgress();
            if (!progress.isEmpty()) {

                progress.forEach((key, p) -> {
                    JobType jobType = JobType.getConstantDictionaryContentMap2().get(key.getTypeId());

                    if (isNull(jobType.getIsVolatile(), false) && p.getState() == JobState.COMPLETED
                        && Duration.between(p.getTimestamp(), LocalDateTime.now()).toMillis() > 30000) {
                        removeJob(key);
                        jobDao.delete(key);
                    }
                });
            }
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    public Map<JobKey, JobProgress> getJobProgress() {
        return monitorService.getProgress();
    }

    private void startJob(JobRunner runner, JobKey jobKey, final JobParameters parameters) {
        logger.debug("starting job: " + jobKey);
        if (jobs.containsKey(jobKey)) {
            throw new JobException("job already exists: " + jobKey);
        }

        SoftCancelState cancelState = new SoftCancelState();
        ListenableFuture<JobProgress> result = runner.runImpersonated(jobKey, monitorService, cancelState, parameters);

        result.addCallback(
                // callback-и выполняются в треде runner-а
                data -> {
                    JobType jobType = JobType.getConstantDictionaryContentMap2().get(data.getJobKey().getTypeId());

                    if (cancelState.isOperationInterrupted()) {
                        logger.debug("Job cancelled: " + data.getJobKey());

                        monitorService.markRemovingJob(data.getJobKey());

                        try {
                            jobDao.setCanceled(data.getJobKey(), "Операция прервана", data.getResult());
                        } catch (Exception e) {
                            logger.error("Exception saving job", e);
                        }

                        removeJob(data.getJobKey());
                    } else {
                        logger.debug("Job completed: " + data.getJobKey());

                        try {
                            if (isNull(jobType.getIsVolatile(), false)) {
                                monitorService.reportCompleted(data.getJobKey(), data.getResult());
                            } else {
                                monitorService.markRemovingJob(data.getJobKey());
                                jobDao.setCompleted(data);
                                removeJob(data.getJobKey());
                            }
                        } catch (Exception e) {
                            logger.error("Exception saving job", e);
                            removeJob(data.getJobKey());
                        }
                    }

                },
                ex -> {

                    monitorService.markRemovingJob(jobKey);

                    try {
                        if (ex instanceof CancellationException) {
                            logger.debug("Job canceled: " + jobKey);

                            jobDao.setCanceled(jobKey, ex.getMessage());
                        } else {
                            logger.debug("Job failed: " + jobKey, ex);

                            jobDao.setFailed(jobKey, exceptionTranslator.translateToUser(ex));
                        }

                        // какие-то исключения, которые не касаются бизнес-правил, пишем также в лог, чтобы по логам анализ можно
                        //было проводить
                        if (!(ex instanceof BusinessLogicException)) {
                            logger.error("Непредвиденная ошибка при выполнении операции: " + exceptionTranslator.translateToUser(ex), ex);
                        }
                    } catch (Exception e) {
                        logger.error("Exception saving job", e);
                    }

                    removeJob(jobKey);
                });

        addJob(jobKey, result, cancelState);

    }

    private void addJob(JobKey jobKey, ListenableFuture<JobProgress> future, SoftCancelState cancelState) {
        jobs.put(jobKey, new ListenableFutureState<>(future, cancelState));
        monitorService.addJob(jobKey);
    }

    private void removeJob(JobKey jobKey) {
        jobs.remove(jobKey);
        monitorService.removeJob(jobKey);
    }

    public void cancel(JobKey key) {

        cancel(key, false);
    }

    public void softCancel(JobKey key) {

        cancel(key, true);
    }

    private void cancel(JobKey key, boolean softCancelling) {

        synchronized (jobs) {
            ListenableFutureState<JobProgress> future = jobs.get(key);
            if (future != null) {
                if (softCancelling) {
                    future.getCancelState().requestCancellation();
                } else {
                    future.getFuture().cancel(true);
                }
            }
        }
    }

    public void cancelAll(boolean softCanceling) {
        synchronized (jobs) {
            jobs.forEach((id, result) -> {
                if (softCanceling) {
                    result.getCancelState().requestCancellation();
                } else {
                    result.getFuture().cancel(true);
                }
            });
        }
    }

    // Unit Testing
    public void waitForAll() throws InterruptedException {
        while (!jobs.isEmpty()) {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
    }
}
