package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.Getter;

public class SoftCancelState {
    @Getter
    private boolean cancelRequested;
    @Getter
    private boolean operationInterrupted;

    public void requestCancellation() {
        cancelRequested = true;
    }

    public void markInterrupted() {
        operationInterrupted = true;
    }
}
