package ru.audithon.egissostat.infrastructure.mass.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;
import ru.audithon.egissostat.infrastructure.AppCacheControl;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;
import ru.audithon.egissostat.infrastructure.mass.domain.SoftCancelState;
import ru.audithon.egissostat.jobs.JobParameters;

public abstract class JobRunner {

    private final JobContextService jobContextService;

    @Autowired
    private AppCacheControl cacheControl;

    @Autowired
    public JobRunner(JobContextService jobContextService) {
        this.jobContextService = jobContextService;
    }

    protected abstract String run(JobKey jobKey, JobProgressMonitor progressMonitor, SoftCancelState cancelState,
                                  JobParameters parametersBase);

    @Async("jobExecutor")
    public ListenableFuture<JobProgress> runImpersonated(JobKey jobKey, JobProgressMonitor progressMonitor,
                                                         SoftCancelState cancelState, JobParameters parametersBase) {

        jobContextService.initContextAtJobStart(jobKey);
        try {
            try {
                String result = run(jobKey, progressMonitor, cancelState, parametersBase);
                return AsyncResult.forValue(JobProgress.completed(jobKey, result));
            } finally {
                cacheControl.cleanupRepositoryStashes();
            }
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        } finally {
            // отдали при запуске контекстные данные дочернему потоку и зачищаем в текущем,
            // чтобы они этот поток дальше не шарил их кому не надо
            jobContextService.finalizeContextAtJobEnd();
        }
    }
}
