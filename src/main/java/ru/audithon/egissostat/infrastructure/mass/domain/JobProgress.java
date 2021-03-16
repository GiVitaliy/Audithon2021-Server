package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JobProgress {
    private final JobKey jobKey;

    private final int progress;
    private final LocalDateTime timestamp;
    private final int state;
    private final String message;
    private final String result;

    public static JobProgress zero(JobKey jobKey) {
        return new JobProgress(jobKey, 0, LocalDateTime.now(), JobState.NEW, null, null);
    }

    public static JobProgress completed(JobKey jobKey, String result) {
        return new JobProgress(jobKey, 10000, LocalDateTime.now(), JobState.COMPLETED, null, result);
    }

    public static JobProgress interrupted(JobKey jobKey) {
        return new JobProgress(jobKey, 10000, LocalDateTime.now(), JobState.CANCELED, null, null);
    }

    public static JobProgress of(JobKey jobKey, LocalDateTime timestamp, int total, int current, int jobState, String message) {
        return new JobProgress(jobKey, calculateProgress(total, current), timestamp, jobState, message, null);
    }

    public static JobProgress of(JobKey jobKey, int total, int current, int jobState, String message) {
        return of(jobKey, LocalDateTime.now(), total, current, jobState, message);
    }

    public static JobProgress of(JobKey jobKey, int progress, int jobState, String message) {
        return new JobProgress(jobKey, progress, LocalDateTime.now(), jobState, message, null);
    }

    private static int calculateProgress(int total, int current) {
        return (int)(10000 *(double)current/total);
    }
}
