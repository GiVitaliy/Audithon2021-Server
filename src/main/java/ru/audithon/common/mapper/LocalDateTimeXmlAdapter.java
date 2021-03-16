package ru.audithon.common.mapper;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeXmlAdapter extends XmlAdapter<String, LocalDateTime> {

    public LocalDateTime unmarshal(String v) throws Exception {
        int ix = v.indexOf("+");
        if (ix >= 0) {
            v = v.substring(0, ix);
        }
        return LocalDateTime.parse(v);
    }

    public String marshal(LocalDateTime v) throws Exception {
        return v.toString();
    }
}