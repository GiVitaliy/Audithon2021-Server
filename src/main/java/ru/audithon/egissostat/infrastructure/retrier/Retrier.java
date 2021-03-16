package ru.audithon.egissostat.infrastructure.retrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.audithon.egissostat.infrastructure.TransactionalWrapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Service
public class Retrier {
    private final TransactionalWrapper transactionalWrapper;

    private final Random r = new Random();
    private final Set<RetryListener> retryListeners = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(Retrier.class);


    @Autowired
    public Retrier(TransactionalWrapper transactionalWrapper) {
        this.transactionalWrapper = transactionalWrapper;
    }

    public void registerListener(RetryListener listener) {
        Objects.requireNonNull(listener);

        retryListeners.add(listener);
    }

    public void unregisterListener(RetryListener listener) {
        Objects.requireNonNull(listener);

        retryListeners.remove(listener);
    }

    public void execute(Runnable action, Predicate<Throwable> retryChecker, int maxAttempts, TransactionStrategy transactional) {
        Objects.requireNonNull(action);
        Objects.requireNonNull(retryChecker);
        Objects.requireNonNull(transactional);

        int attemptsCount = 1;

        while(true){
            try {
                switch(transactional){
                    case NoTran: action.run(); break;
                    case Required: transactionalWrapper.doWithRequiredTransaction(action); break;
                    case RequiresNew: transactionalWrapper.doWithNewTransaction(action); break;
                    default: throw new IllegalArgumentException(
                            String.format("Необработанное значение параметра transactional: %s", transactional));
                }

                break;
            } catch(Exception e) {
                logger.debug("execute", e);

                if (!retryChecker.test(e))
                    throw e;

                if (!checkAttemptsCount(maxAttempts, attemptsCount++))
                    throw new AttemptsMaxCountExceeded(maxAttempts, e);

                sendEvent();
            }
        }
    }

    public <T> T get(Supplier<T> supplier, Predicate<Throwable> retryChecker, int maxAttempts, TransactionStrategy transactional){
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(retryChecker);
        Objects.requireNonNull(transactional);

        int attemptsCount = 1;

        while(true){
            try {
                switch(transactional){
                    case NoTran: return supplier.get();
                    case Required: return transactionalWrapper.getWithRequiredTransaction(supplier);
                    case RequiresNew: return transactionalWrapper.getWithNewTransaction(supplier);
                    default: throw new IllegalArgumentException(
                            String.format("Необработанное значение параметра transactional: %s", transactional));
                }
            } catch(Exception e) {
                logger.debug("get", e);

                if (!retryChecker.test(e))
                    throw e;

                if (!checkAttemptsCount(maxAttempts, attemptsCount++))
                    throw new AttemptsMaxCountExceeded(maxAttempts, e);

                sendEvent();
            }
        }
    }

    public <T> T getWithRollback(Supplier<T> supplier, Predicate<Throwable> retryChecker, int maxAttempts, TransactionStrategy transactional){
        AtomicReference<T> resultWrapper = new AtomicReference<>();
        try {
            Runnable action = () -> {
                T result = get(supplier, retryChecker, maxAttempts, transactional);
                resultWrapper.set(result);
                throw new RollbackModeException();
            };

            execute(action, retryChecker, maxAttempts, transactional);
        } catch(RollbackModeException ex) {
            //ожидаемая ошибка отката - она нам и нужна, чтобы ничего с ней не делать
        }

        return resultWrapper.get();
    }

    public enum TransactionStrategy {
        NoTran,
        Required,
        RequiresNew
    }

    private boolean checkAttemptsCount(int maxAttempts, int attemptsCount) {
        if (attemptsCount < maxAttempts) {
            return true;
//            try {
//                Thread.sleep(RandomUtils.getRandomNumberInRange(5, 50));
//                return true;
//            } catch(InterruptedException ex) {
//                return false;
//            }
        }
        return false;
    }

    private void sendEvent(){
        for (RetryListener retryListener : retryListeners) {
            retryListener.retryOccuried();
        }
    }
}
