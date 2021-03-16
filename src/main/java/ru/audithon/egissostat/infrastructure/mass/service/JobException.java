package ru.audithon.egissostat.infrastructure.mass.service;

public class JobException extends RuntimeException {
    public JobException() {
    }

    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
