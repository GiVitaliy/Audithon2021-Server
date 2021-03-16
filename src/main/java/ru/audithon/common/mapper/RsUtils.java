package ru.audithon.common.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class RsUtils {
    public static Short getNullableShort(ResultSet rs, int columnIndex) throws SQLException {
        short value = rs.getShort(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Short getNullableShort(ResultSet rs, String columnLabel) throws SQLException {
        short value = rs.getShort(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Integer getNullableInt(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Integer getNullableInt(HashMap<String, Object> rs, String columnLabel) {
        return rs.get(columnLabel) != null ? Integer.valueOf(rs.get(columnLabel).toString()) : null;
    }

    public static Date getNullableDate(ResultSet rs, String columnLabel) throws SQLException {
        Date value = rs.getDate(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Long getNullableLong(ResultSet rs, int columnIndex) throws SQLException {
        long value = rs.getLong(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Long getNullableLong(ResultSet rs, String columnLabel) throws SQLException {
        long value = rs.getLong(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Long getNullableLong(HashMap<String, Object> rs, String columnLabel) {
        return rs.get(columnLabel) != null ? Long.valueOf(rs.get(columnLabel).toString()) : null;
    }

    public static Boolean getNullableBoolean(ResultSet rs, int columnIndex) throws SQLException {
        boolean value = rs.getBoolean(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Boolean getNullableBoolean(ResultSet rs, String columnLabel) throws SQLException {
        boolean value = rs.getBoolean(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Boolean getNullableBoolean(HashMap<String, Object> rs, String columnLabel) {
        Boolean value = (Boolean) rs.get(columnLabel);
        if (value == null) {
            return null;
        }

        return value;
    }

    public static Float getNullableFloat(ResultSet rs, int columnIndex) throws SQLException {
        float value = rs.getFloat(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Float getNullableFloat(ResultSet rs, String columnLabel) throws SQLException {
        float value = rs.getFloat(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static BigDecimal getNullableDecimal(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static BigDecimal getNullableDecimal(HashMap<String, Object> rs, String columnLabel) {
        return (BigDecimal) rs.get(columnLabel);
    }

    public static BigDecimal getNullableDecimal(ResultSet rs, String columnLabel) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static BigInteger getNullableBigInteger(ResultSet rs, int columnIndex) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value.toBigInteger();
    }

    public static BigInteger getNullableBigInteger(ResultSet rs, String columnLabel) throws SQLException {
        BigDecimal value = rs.getBigDecimal(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value.toBigInteger();
    }

    public static Double getNullableDouble(ResultSet rs, int columnIndex) throws SQLException {
        double value = rs.getDouble(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static Double getNullableDouble(ResultSet rs, String columnLabel) throws SQLException {
        double value = rs.getDouble(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static UUID getUuid(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        if (rs.wasNull()) {
            return null;
        }

        return (UUID) value;
    }

    public static UUID getUuid(ResultSet rs, String columnLabel) throws SQLException {
        Object value = rs.getObject(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return (UUID) value;
    }

    public static LocalDate getLocalDate(ResultSet rs, int columnIndex) throws SQLException {
        LocalDate value = rs.getObject(columnIndex, LocalDate.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static LocalDate getLocalDate(ResultSet rs, String columnLabel) throws SQLException {
        LocalDate value = rs.getObject(columnLabel, LocalDate.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static LocalDate getLocalDate(HashMap<String, Object> rs, String columnLabel) {
        java.sql.Date value = (java.sql.Date) rs.get(columnLabel);
        if (value == null) {
            return null;
        }

        return value.toLocalDate();
    }

    public static Date getUtilDate(HashMap<String, Object> rs, String columnLabel) {
        java.sql.Date value = (java.sql.Date) rs.get(columnLabel);
        if (value == null) {
            return null;
        }

        return value;
    }

    // данный метод работает нормально например с ms sql server jdbc driver
    public static LocalDate getLocalDate2(ResultSet rs, String columnLabel) throws SQLException {
        java.sql.Date value = rs.getDate(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return value.toLocalDate();
    }

    public static LocalDate getLocalDate2(HashMap<String, Object> rs, String columnLabel) {
        if (rs.get(columnLabel) instanceof LocalDate) {
            return (LocalDate) rs.get(columnLabel);
        }

        java.sql.Date value = (java.sql.Date) rs.get(columnLabel);
        if (value == null) {
            return null;
        }

        return value.toLocalDate();
    }

    // данный метод работает нормально например с ms sql server jdbc driver
    public static LocalDateTime getLocalDateTime2(ResultSet rs, String columnLabel) throws SQLException {
        java.sql.Date value = rs.getDate(columnLabel);
        if (rs.wasNull()) {
            return null;
        }

        return Instant.ofEpochMilli(value.getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    public static LocalDateTime getLocalDateTime2(HashMap<String, Object> rs, String columnLabel) {
        java.sql.Timestamp value = (java.sql.Timestamp) rs.get(columnLabel);
        if (value == null) {
            return null;
        }

        return Instant.ofEpochMilli(value.getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    public static LocalDate getLocalDate2(ResultSet rs, String columnLabel, LocalDate ifNullValue) throws SQLException {
        LocalDate val = getLocalDate2(rs, columnLabel);
        if (val == null)
            val = ifNullValue;
        return val;
    }

    public static LocalDateTime getLocalDateTime(ResultSet rs, int columnIndex) throws SQLException {
        LocalDateTime value = rs.getObject(columnIndex, LocalDateTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static LocalDateTime getLocalDateTime(ResultSet rs, String columnLabel) throws SQLException {
        LocalDateTime value = rs.getObject(columnLabel, LocalDateTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static LocalTime getLocalTime(ResultSet rs, int columnIndex) throws SQLException {
        LocalTime value = rs.getObject(columnIndex, LocalTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static LocalTime getLocalTime(ResultSet rs, String columnLabel) throws SQLException {
        LocalTime value = rs.getObject(columnLabel, LocalTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static OffsetDateTime getOffsetDateTime(ResultSet rs, int columnIndex) throws SQLException {
        OffsetDateTime value = rs.getObject(columnIndex, OffsetDateTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static OffsetDateTime getOffsetDateTime(ResultSet rs, String columnLabel) throws SQLException {
        OffsetDateTime value = rs.getObject(columnLabel, OffsetDateTime.class);
        if (rs.wasNull()) {
            return null;
        }

        return value;
    }

    public static ArrayList<HashMap<String, Object>> resultSetToArrayList(ResultSet rs, int capacity) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        ArrayList<HashMap<String, Object>> list = new ArrayList<>(10000000);
        while (rs.next()) {
            HashMap<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                if (rs.getObject(i) instanceof java.sql.Date) {
                    row.put(md.getColumnName(i), rs.getObject(i, LocalDate.class));
                } else {
                    row.put(md.getColumnName(i), rs.getObject(i));
                }
            }
            list.add(row);
        }

        return list;
    }

    public static RowMapper<HashMap<String, Object>> getRsToHashMapper() {
        return (rs, rowNo) -> {

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();

            HashMap<String, Object> row = new HashMap<>(columns);
            for(int i=1; i<=columns; ++i){
                row.put(md.getColumnName(i),rs.getObject(i));
            }

            return row;
        };
    }

    public static String getString(HashMap<String, Object> rs, String columnLabel) {
        return rs.get(columnLabel) == null ? null : rs.get(columnLabel).toString();
    }
}
