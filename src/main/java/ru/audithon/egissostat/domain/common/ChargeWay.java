package ru.audithon.egissostat.domain.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

public enum ChargeWay {
    MONTHLY(1),
    ONE_TIME(2);

    private int value;
    private final static Map<Integer, ChargeWay> map = new HashMap<>();

    ChargeWay(int value) {
        this.value = value;
    }

    static {
        for (ChargeWay chargeWay : ChargeWay.values()) {
            map.put(chargeWay.value, chargeWay);
        }
    }

    @JsonCreator
    public static ChargeWay valueOfJson(String chargeWay) {
        if (Strings.isNullOrEmpty(chargeWay))
            return null;

        return ChargeWay.valueOf(Integer.parseInt(chargeWay));
    }

    public static ChargeWay valueOf(Integer chargeWay) {
        return chargeWay != null ? map.get(chargeWay) : null;
    }


    @JsonValue
    public int getValue() {
        return value;
    }
}
