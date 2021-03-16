package ru.audithon.common.mapper;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RusBoolXmlAdapter extends XmlAdapter<String, Boolean> {
    public Boolean unmarshal(String v) throws Exception {
        if (Strings.isNullOrEmpty(v))
            return  null;

        return v.equalsIgnoreCase("Да");
    }

    public String marshal(Boolean v) throws Exception {
        if (v == null)
            return null;

        return v ? "Да" : "Нет";
    }
}