package ru.audithon.common.helpers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class SqlStringBuilderUtils {

    public static String objectToSqlLiteral(Object obj) {
        if (obj == null) {
            return "NULL";
        } else if (obj instanceof LocalDate) {
            return getSqlLiteral((LocalDate) obj);
        } else if (obj instanceof String) {
            return getSqlLiteral((String) obj);
        } else if (obj instanceof Integer) {
            return getSqlLiteral((Integer) obj);
        } else if (obj instanceof Boolean) {
            return getSqlLiteral((Boolean) obj);
        } else if (obj instanceof Long) {
            return getSqlLiteral((Long) obj);
        } else {
            return getSqlLiteral(obj.toString());
        }
    }

    public static String getSqlLiteral(LocalDate date) {
        return date != null ? "'" + date.format(DateTimeFormatter.ISO_DATE) + "'" : "NULL";
    }

    public static String getSqlLiteral(BigDecimal value) {
        return value != null ? value.toString() : "NULL";
    }

    public static String getSqlLiteral(String str) {
        // тут мы не будем подбирать рандомный тег, а просто заюзаем фиксированный. если кто-то захочет использовать его
        // в строке - он просто тупо не сможет
        String dollarTag = "$t581754$";
        if (str != null && str.contains(dollarTag)) {
            throw new RuntimeException("Trying SQL-Injection detected!!!");
        }
        return str != null ? dollarTag + str + dollarTag : "NULL";
    }

    public static String getSqlLiteral(Boolean boo) {
        return boo != null ? (boo ? "TRUE" : "FALSE") : "NULL";
    }

    public static String getSqlLiteral(Integer i) {
        return i != null ? i.toString() : "NULL";
    }

    public static String getSqlLiteral(Long i) {
        return i != null ? i.toString() : "NULL";
    }

    public static String getSqlLiteral(Collection<Integer> collection) {
        if (collection == null || collection.size() == 0) {
            return "NULL";
        }
        StringBuilder retVal = new StringBuilder();
        collection.forEach(el -> {
            retVal.append(el);
            retVal.append(",");
        });
        retVal.deleteCharAt(retVal.length() - 1);
        return retVal.toString();
    }
}
