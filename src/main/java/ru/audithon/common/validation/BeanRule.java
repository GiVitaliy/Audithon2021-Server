package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BeanRule<TObject> implements Rule<TObject> {
    private final Function<TObject, Boolean> predicate;
    private final String messageKey;

    public BeanRule(Function<TObject, Boolean> predicate, String messageKey) {
        Objects.requireNonNull(predicate);

        this.predicate = predicate;
        this.messageKey = messageKey;
    }

    public Optional<BeanRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        Boolean result = predicate.apply(object);
        if (result == null || !result) {
            String message = messageSource.getMessage(messageKey, new Object[] {}, messageKey, Locale.getDefault());

            return Optional.of(new BeanRuleViolation(message));
        }

        return Optional.empty();
    }
}
