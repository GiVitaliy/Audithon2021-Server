package ru.audithon.common.mapper;

import com.google.common.base.Strings;
import ru.audithon.common.helpers.DateUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateDotNetInvariantCultureXmlAdapter extends XmlAdapter<String, LocalDate> {

    public LocalDate unmarshal(String v) {
        if (Strings.isNullOrEmpty(v)) {
            return null;
        }

        if (v.length() > 10) {
            try {
                return DateUtils.parseXmlDateTime(v).toLocalDate();
            } catch (Exception ignored) {
            }

            return LocalDateTime.parse(v, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
        }

        try {
            return DateUtils.parseXmlDate(v);
        } catch (Exception ignored) {
        }

        return DateUtils.parseISODate(v);
    }

    public String marshal(LocalDate v) {
        return DateUtils.formatISODate(v);
    }
}