package ru.audithon.common.helpers;

import com.google.common.primitives.Longs;

public class LongUtils {

    public static Long tryParse(String value) {
        if (StringUtils.isNullOrWhitespace(value)) {
            return null;
        }

        return Longs.tryParse(value);
    }

}
