package ru.audithon.egissostat.infrastructure.mass.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.egissostat.infrastructure.mass.domain.JobProgress;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class JobProgressMonitorImpl implements JobProgressMonitorControl {

    private final ConcurrentMap<JobKey, JobProgress> currentState = new ConcurrentHashMap<>();
    private final ConcurrentMap<JobKey, Boolean> removingJobs = new ConcurrentHashMap<>();

    @Override
    public void reportProgress(JobKey jobKey, int total, int current, int jobState, String message) throws InterruptedException {
        reportProgress(jobKey, LocalDateTime.now(), total, current, jobState, message);
    }

    @Override
    public void reportProgress(JobKey jobKey, LocalDateTime timestamp, int total, int current,
                               int jobState, String message) throws InterruptedException {

        if (total <= 0 || total < current) {
            throw new RuntimeException("Некорректное значение прогресса операции, total = " + total + ", current = " + current);
        }

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Обнаружено изменение статуса прерванной операции");
        }

        currentState.replace(jobKey, JobProgress.of(jobKey, timestamp, total, current, jobState, message));
    }

    @Override
    public void reportCompleted(JobKey jobKey, String result) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Обнаружено изменение статуса прерванной операции");
        }

        currentState.replace(jobKey, JobProgress.completed(jobKey, result));
    }

    @Override
    public void addJob(JobKey jobKey) {
        currentState.putIfAbsent(jobKey, JobProgress.zero(jobKey));
    }

    @Override
    public void removeJob(JobKey jobKey) {
        removingJobs.remove(jobKey);
        currentState.remove(jobKey);
    }

    @Override
    @SneakyThrows
    public void markRemovingJob(JobKey jobKey) {

        // метод нужен для того, чтобы запретить какие-либо действия по сохранению состояния, или использованию данных
        // джоба. запускается перед тем, как записать у джоба финальное состояние. не придумал другого способа, как сделать надежную
        // работу блока, не переписав целиком систему записи состояния в БД

        removingJobs.putIfAbsent(jobKey, true);
        // так топорненько подождем, чтобы уменьшить вероятность, что отмечаемый джоб уже прямо щас в другом потоке
        // сохраняется и это состояние перетрет то состояние, что запишет после вызова клиент метода markRemovingJob()
        Thread.sleep(200L);
    }

    @Override
    public Map<JobKey, JobProgress> getProgress() {
        Map<JobKey, JobProgress> retVal = new HashMap<>(currentState);

        // не показываем удаляемые в настояший момент джобы - чтобы они уже нигде не всплывали, в том числе при сохранении
        //состояния
        new ArrayList<>(retVal.values()).forEach(x-> {
            if (removingJobs.containsKey(x.getJobKey())) {
                retVal.remove(x.getJobKey());
            }
        });

        return retVal;
    }

    @Override
    public Optional<JobProgress> getProgress(JobKey jobKey) {
        return Optional.ofNullable(currentState.get(jobKey));
    }
}
