package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.Getter;
import org.springframework.util.concurrent.ListenableFuture;

public class ListenableFutureState<V> {
    @Getter
    private final ListenableFuture<V> future;
    @Getter
    private final SoftCancelState cancelState;

    public ListenableFutureState(ListenableFuture<V> future, SoftCancelState cancelState) {
        this.future = future;
        this.cancelState = cancelState;
    }
}
