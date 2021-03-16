package ru.audithon.common.validation;

import java.util.function.Function;

public interface NamedFunction<T, V> extends Function<T, V> {
    String getMessageKey();
    Object[] getValues();

    boolean valueOnNull();
}
