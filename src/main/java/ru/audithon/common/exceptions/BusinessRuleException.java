package ru.audithon.common.exceptions;

import com.google.common.base.MoreObjects;
import ru.audithon.common.validation.RuleViolationData;

public abstract class BusinessRuleException extends RuntimeException {
    private final RuleViolationData data;

    protected BusinessRuleException(RuleViolationData data) {
        super("BusinessRuleException");
        this.data = data;
    }

    protected BusinessRuleException( String messageKey) {
        this( messageKey, new Object[] {});
    }

    protected BusinessRuleException( String messageKey, Object messageValue) {
        this( messageKey, new Object[] { messageValue });
    }

    protected BusinessRuleException(String messageKey, Object[] messageValues) {
        super("BusinessRuleException");
        this.data = new RuleViolationData(messageKey, messageValues);
    }

    public RuleViolationData getData() {
        return data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("data", data)
            .toString();
    }
}
