package ru.audithon.common.types;

import lombok.Data;

@Data
public class NumberWrapper<T extends Number>{
    private T value;
    public NumberWrapper(T value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        if(value != null)
            return String.valueOf(value);

        return null;
    }

    @SuppressWarnings("unchecked")
    public void inc(T increment)
    {
        if (value instanceof Byte) {
            value = (T)(Byte.valueOf((byte)(value.byteValue() + increment.byteValue())));
            return;
        }
        if (value instanceof Short)
        {
            value = (T)(Short.valueOf((short)(value.shortValue() + increment.shortValue())));
            return;
        }
        if (value instanceof Integer)
        {
            value = (T)(Integer.valueOf(value.intValue() + increment.intValue()));
            return;
        }
        if (value instanceof Long)
        {
            value = (T)(Long.valueOf(value.longValue() + increment.longValue()));
            return;
        }
        if (value instanceof Float)
        {
            value = (T)(Float.valueOf((value.floatValue() + increment.floatValue())));
            return;
        }
        if (value instanceof Double)
        {
            value = (T)(Double.valueOf((value.doubleValue() + increment.doubleValue())));
        }
    }
}
