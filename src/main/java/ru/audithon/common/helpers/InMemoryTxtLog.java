package ru.audithon.common.helpers;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Supplier;

public class InMemoryTxtLog {

    private static DateTimeFormatter logStrDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private boolean isActive;
    private StringBuilder log = new StringBuilder();

    public InMemoryTxtLog() {
        isActive = true;
    }

    public InMemoryTxtLog(boolean isActive) {
        this.isActive = isActive;
    }

    public void addLogRecord(String str) {
        if (!isActive) return;

        log.append(LocalDateTime.now().format(logStrDateFormatter));
        log.append(" ");
        log.append(str);
        log.append("\r\n");
    }

    public void addLogRecord(String strFormat, Object... parameters) {
        addLogRecord(String.format(strFormat, parameters));
    }

    public byte[] getUtf8LogBytes() {
        return log.toString().getBytes(Charset.forName("UTF-8"));
    }

    public String getLogString() { return log.toString(); }

    public static void addLogRecord(InMemoryTxtLog log, String str) {
        if (log != null) {
            log.addLogRecord(str);
        }
    }

    public static void addLogRecord(InMemoryTxtLog log, String strFormat, Object... parameters) {
        if (log != null) {
            log.addLogRecord(strFormat, parameters);
        }
    }

    public static void addLogRecordWithCaption(InMemoryTxtLog log, String caption, String str) {
        if (log != null) {
            log.addLogRecord(caption + " : " + str);
        }
    }

    public static void addLogRecordWithCaption(InMemoryTxtLog log, String caption, String strFormat, Object... parameters) {
        if (log != null) {
            log.addLogRecord(caption + " : " + String.format(strFormat, parameters));
        }
    }

    public static void addLogRecordWithCaption(InMemoryTxtLog log, String caption, Supplier<String> str) {
        if (log != null && str != null) {
            log.addLogRecord(caption + " : " + str.get());
        }
    }

    @SafeVarargs
    public static void addLogRecordWithCaption(InMemoryTxtLog log, String caption, String strFormat, Supplier<Object>... parameterSupplier) {
        if (log != null) {
            Object[] parameters = new Object[parameterSupplier.length];
            for(int i = 0; i < parameters.length; i++) {
                parameters[i] = parameterSupplier[i].get();
            }
            log.addLogRecord(caption + " : " + String.format(strFormat, parameters));
        }
    }
}
