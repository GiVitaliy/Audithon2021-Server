package ru.audithon.common.validation;

import java.util.function.Function;

public class NamedFunctionImpl<T, V> implements NamedFunction<T, V> {

    private final Function<T, V> function;
    private final String messageKey;
    private final boolean valueOnNull;
    private final Object[] values;

    public NamedFunctionImpl(Function<T, V> function, String messageKey) {
        this(function, messageKey, null);
    }

    public NamedFunctionImpl(Function<T, V> function, String messageKey, Object[] values) {
        this(function, messageKey, true, values);
    }

    public NamedFunctionImpl(Function<T, V> function, String messageKey, boolean valueOnNull) {
        this(function, messageKey, valueOnNull, null);
    }

    public NamedFunctionImpl(Function<T, V> function, String messageKey, boolean valueOnNull, Object[] values) {
        this.function = function;
        this.messageKey = messageKey;
        this.valueOnNull = valueOnNull;
        this.values = values == null ? new Object[] {} : values;
    }

    @Override
    public String getMessageKey() {
        return messageKey;
    }

    @Override
    public Object[] getValues() {
        return values;
    }

    @Override
    public V apply(T t) {
        return function.apply(t);
    }

    @Override
    public boolean valueOnNull() {
        return valueOnNull;
    }
}
