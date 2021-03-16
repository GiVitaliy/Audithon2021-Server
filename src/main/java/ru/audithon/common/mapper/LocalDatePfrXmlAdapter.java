package ru.audithon.common.mapper;

import ru.audithon.common.helpers.StringUtils;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDatePfrXmlAdapter extends XmlAdapter<String, LocalDate> {
    private static final DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public LocalDate unmarshal(String v) {
        if (StringUtils.isNullOrWhitespace(v)) {
            return null;
        }

        return LocalDate.parse(v, formatterDate);
    }

    public String marshal(LocalDate v) {
        return formatterDate.format(v);
    }
}
