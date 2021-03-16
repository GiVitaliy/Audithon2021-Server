package ru.audithon.egissostat.infrastructure;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Service
public class TransactionalWrapper {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void doWithNewTransaction(Runnable action){
        action.run();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void doWithRequiredTransaction(Runnable action){
        action.run();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void doWithNestedTransaction(Runnable action){
        action.run();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T getWithNewTransaction(Supplier<T> supplier){
        return supplier.get();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public <T> T getWithRequiredTransaction(Supplier<T> supplier){
        return supplier.get();
    }

    @Transactional(propagation = Propagation.NESTED)
    public <T> T getWithNestedTransaction(Supplier<T> supplier){
        return supplier.get();
    }
}
