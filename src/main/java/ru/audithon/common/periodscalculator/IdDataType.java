package ru.audithon.common.periodscalculator;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class IdDataType<T> {
    private int dataId;

    public static <T> OffsetIdDataTypeGetter<T> newOffsetIdDataTypeGetter(int baseId) {
        return offset -> new IdDataType<>(baseId + offset);
    }
}
