package ru.audithon.egissostat.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.audithon.common.validation.RuleViolationData;

import java.util.Locale;
import java.util.Objects;

@Service
public class MessageTranslator {
    private final MessageSource messageSource;

    @Autowired
    public MessageTranslator(MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        this.messageSource = messageSource;
    }

    public String translate(String messageKey, Object[] data) {
        return messageSource.getMessage(messageKey, data, messageKey, Locale.getDefault());
    }

    public String translate(RuleViolationData data) {
        return messageSource.getMessage(data.getMessageKey(), data.getMessageValues(), Locale.getDefault());
    }
}
