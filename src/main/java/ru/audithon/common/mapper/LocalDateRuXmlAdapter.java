package ru.audithon.common.mapper;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateRuXmlAdapter extends XmlAdapter<String, LocalDate> {

    public static final DateTimeFormatter ruDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public LocalDate unmarshal(String v) {
        return LocalDate.parse(v, ruDateFormatter);
    }

    public String marshal(LocalDate v) {
        return ruDateFormatter.format(v);
    }
}