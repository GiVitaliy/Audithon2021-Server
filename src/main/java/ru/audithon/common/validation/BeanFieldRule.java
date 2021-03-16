package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BeanFieldRule<TObject> implements Rule<TObject> {
    private final Function<TObject, Boolean> predicate;
    private final String messageKey;
    private final String fieldName;
    private final String relationName;

    public BeanFieldRule(Function<TObject, Boolean> predicate,
                         String fieldName, String relationName,
                         String messageKey) {
        Objects.requireNonNull(predicate);

        this.predicate = predicate;
        this.messageKey = messageKey;
        this.fieldName = fieldName;
        this.relationName = relationName;
    }

    public Optional<FieldRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        Boolean result = predicate.apply(object);
        if (result == null || !result) {
            String message = messageSource.getMessage(messageKey, new Object[] {}, messageKey, Locale.getDefault());

            return Optional.of(new FieldRuleViolation(fieldName, relationName, message));
        }

        return Optional.empty();
    }
}
