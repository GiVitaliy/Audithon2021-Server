package ru.audithon.common.validation;

import lombok.Data;

@Data
public class RuleViolationData {
    private final String messageKey;
    private final Object[] messageValues;

    public RuleViolationData(String messageKey, Object[] messageValues) {
        this.messageKey = messageKey;
        this.messageValues = messageValues;
    }

    public RuleViolationData(String messageKey) {
        this(messageKey, new Object[] {});
    }

    public RuleViolationData(String messageKey, Object messageValue) {
        this(messageKey, new Object[] {messageValue});
    }


}
