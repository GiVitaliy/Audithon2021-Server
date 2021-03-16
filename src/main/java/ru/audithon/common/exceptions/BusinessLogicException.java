package ru.audithon.common.exceptions;

public class BusinessLogicException extends RuntimeException {
    private String messageKey;

    public String getMessageKey() {
        return this.messageKey;
    }

    public BusinessLogicException() {
    }

    public BusinessLogicException(String message) {
        this(message, null);
    }

    public BusinessLogicException(String messageKey, String messageFormat, Object... args) {
        this(String.format(messageFormat, args), messageKey);
    }

    public BusinessLogicException(String message, String messageKey) {
        super(message);

        this.messageKey = messageKey;
    }
}
