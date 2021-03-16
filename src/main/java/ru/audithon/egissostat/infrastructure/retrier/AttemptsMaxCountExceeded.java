package ru.audithon.egissostat.infrastructure.retrier;

public class AttemptsMaxCountExceeded extends RuntimeException {
    public AttemptsMaxCountExceeded(Throwable cause) {
        super(cause);
    }

    public AttemptsMaxCountExceeded(int maxNumber, Throwable cause) {
        super(String.format(
                "Превышено число попыток (%s) выполнить операцию", maxNumber), cause);
    }
}
