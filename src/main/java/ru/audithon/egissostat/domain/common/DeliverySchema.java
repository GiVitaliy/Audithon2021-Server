package ru.audithon.egissostat.domain.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

public enum DeliverySchema {
    HOME(1),
    OFFICE(2);


    private int value;
    private final static Map<Integer, DeliverySchema> map = new HashMap<>();

    DeliverySchema(int value) {
        this.value = value;
    }

    static {
        for (DeliverySchema schema : DeliverySchema.values()) {
            map.put(schema.value, schema);
        }
    }

    @JsonCreator
    public static DeliverySchema valueOfJson(String schema) {
        if (Strings.isNullOrEmpty(schema))
            return null;

        return DeliverySchema.valueOf(Integer.parseInt(schema));
    }

    public static DeliverySchema valueOf(Integer schema) {
        return schema != null ? map.get(schema) : null;
    }


    @JsonValue
    public int getValue() {
        return value;
    }

    public static Integer getNullableValue(DeliverySchema value){
        return value != null ? value.getValue() : null;
    }
}
