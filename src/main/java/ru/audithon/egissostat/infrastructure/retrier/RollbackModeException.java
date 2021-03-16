package ru.audithon.egissostat.infrastructure.retrier;

public class RollbackModeException extends RuntimeException {
    public RollbackModeException() {
        super("Исключение для отката транзакции");
    }
}
