package ru.audithon.egissostat.infrastructure.mass.service;


import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;

public interface JobProgressMonitorControl extends JobProgressMonitor {
    void addJob(JobKey jobKey);
    void removeJob(JobKey jobKey);
    void markRemovingJob(JobKey jobKey);
}
