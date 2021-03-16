package ru.audithon.common.periodscalculator;

public interface OffsetIdDataTypeGetter<T> {
    IdDataType<T> get(int offset);
}
