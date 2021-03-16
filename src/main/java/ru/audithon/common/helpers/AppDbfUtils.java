package ru.audithon.common.helpers;

import lombok.SneakyThrows;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class AppDbfUtils {
    public static UUID readAsUUID(Object[] row, int index) {
        String str = readAsString(row, index);
        return str != null ? UUID.fromString(readAsString(row, index)) : null;
    }

    @SneakyThrows
    public static String readAsString(Object[] row, int index) {
        if (index >= 0 && index < row.length) {
            return StringUtils.prettify(new String((byte[]) row[index], "Cp866"));
        } else {
            return null;
        }
    }

    public static LocalDate readAsLocalDate(Object[] row, int index) {
        if (index >= 0 && index < row.length && row[index] != null) {
            return ((Date) row[index]).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    public static int readAsInteger(Object[] row, int index) {
        if (index >= 0 && index < row.length && row[index] != null) {

            if (row[index] instanceof Number) {
                return ((Number) row[index]).intValue();
            } else if (row[index] instanceof byte[]) {
                String str = readAsString(row, index);
                if (str == null) {
                    return 0; // считаем пустую строку как null
                }
                return Integer.parseInt(str);
            } else {
                throw new IllegalArgumentException("Некорректное значение");
            }

        } else {
            return 0;
        }
    }
}
