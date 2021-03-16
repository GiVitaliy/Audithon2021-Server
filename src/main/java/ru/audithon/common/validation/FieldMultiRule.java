package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class FieldMultiRule<TObject, TField> implements Rule<TObject> {
    private final String fieldName;
    private final String relationName;
    private final Function<TObject, TField> fieldGetter;
    private final Function<TField, Optional<RuleViolationData>> predicate;

    public FieldMultiRule(String fieldName,
                          String relationName,
                          Function<TObject, TField> fieldGetter,
                          Function<TField, Optional<RuleViolationData>> predicate) {
        this.fieldName = fieldName;
        this.relationName = relationName;
        this.fieldGetter = fieldGetter;
        this.predicate = predicate;
    }

    public Optional<FieldRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        TField fieldValue = fieldGetter.apply(object);

        Optional<RuleViolationData> result = predicate.apply(fieldValue);
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
