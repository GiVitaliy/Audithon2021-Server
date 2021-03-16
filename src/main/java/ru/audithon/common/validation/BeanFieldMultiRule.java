package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BeanFieldMultiRule<TObject> implements Rule<TObject> {
    private final Function<TObject, Optional<RuleViolationData>> predicate;
    private final String fieldName;
    private final String relationName;

    public BeanFieldMultiRule(Function<TObject, Optional<RuleViolationData>> predicate,
                              String fieldName, String relationName) {
        this.predicate = predicate;
        this.fieldName = fieldName;
        this.relationName = relationName;
    }

    public Optional<FieldRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        Optional<RuleViolationData> result = predicate.apply(object);
        if (result.isPresent()) {
            RuleViolationData data = result.get();
            String messageKey = data.getMessageKey();

            String message = messageSource != null ?
                messageSource.getMessage(messageKey, data.getMessageValues(), messageKey, Locale.getDefault())
                : messageKey;

            return Optional.of(new FieldRuleViolation(fieldName, relationName, message));
        }

        return Optional.empty();
    }
}
