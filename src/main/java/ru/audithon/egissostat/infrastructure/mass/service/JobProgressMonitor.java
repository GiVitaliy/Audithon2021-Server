package ru.audithon.egissostat.infrastructure.mass.service;


import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public interface JobProgressMonitor {
    void reportProgress(JobKey job, int total, int current, int jobState, String message) throws InterruptedException;
    void reportProgress(JobKey job, LocalDateTime timestamp, int total, int current,
                        int jobState, String message) throws InterruptedException;
    void reportCompleted(JobKey jobKey, String result) throws InterruptedException;
    Map<JobKey, JobProgress> getProgress();
    Optional<JobProgress> getProgress(JobKey jobKey);
}
