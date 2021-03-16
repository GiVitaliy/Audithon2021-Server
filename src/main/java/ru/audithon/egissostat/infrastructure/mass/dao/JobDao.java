package ru.audithon.egissostat.infrastructure.mass.dao;

import ru.audithon.egissostat.infrastructure.mass.domain.Job;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;
import ru.audithon.common.mapper.CrudDao;

import java.time.LocalDate;
import java.util.List;

public interface JobDao extends CrudDao<Job, JobKey> {
    List<Job> byStateAndNode(int nodeId, int stateId);
    List<Job> byDigestCompleted(String digest);
    List<Job> byDigestLatest10(String digest);
    List<Job> byStateAndCreatedDateRange(Integer stateId, LocalDate dateFrom, LocalDate dateTo, Integer userId);

    int changeStates(int nodeId, int stateFrom, int stateTo);
    int changeStates(int nodeId, int stateFrom, int stateTo, String message);

    int updateProgress(JobProgress progress);

    int setState(JobKey key, int state, String message);

    int setCompleted(JobProgress progress);
    int setFailed(JobKey key, String message);
    int setCanceled(JobKey key, String message);
    int setCanceled(JobKey key, String message, String result);
}
