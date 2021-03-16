package ru.audithon.common.mapper;

import com.google.common.base.Strings;
import ru.audithon.common.helpers.DateUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDotNetInvariantCultureXmlAdapter extends XmlAdapter<String, LocalDateTime> {

    public LocalDateTime unmarshal(String v) {
        if (Strings.isNullOrEmpty(v))
            return null;

        try {
            return DateUtils.parseXmlDateTime(v);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(v, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
        }

        return LocalDate.parse(v, DateTimeFormatter.ISO_DATE).atTime(0, 0, 0);
    }

    public String marshal(LocalDateTime v) {
        return DateUtils.formatISODateTime(v);
    }

}