package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FieldRule<TObject, TField> implements Rule<TObject> {
    private final String fieldName;
    private final String relationMsgKey;
    private final Function<TObject, TField> fieldGetter;
    private final Function<TField, Boolean> predicate;
    private final String messageKey;
    private final Object[] messageValues;
    private final boolean skipNulls;
    private final Predicate<TObject> isActive;

    public FieldRule(String fieldName,
                     String relationMsgKey,
                     Function<TObject, TField> fieldGetter,
                     Function<TField, Boolean> predicate,
                     String messageKey) {
        this(fieldName, relationMsgKey, fieldGetter, predicate, messageKey, null, true, null);
    }

    public FieldRule(String fieldName,
                     String relationMsgKey,
                     Function<TObject, TField> fieldGetter,
                     Function<TField, Boolean> predicate,
                     String messageKey,
                     Object messageValue) {
        this(fieldName, relationMsgKey, fieldGetter, predicate, messageKey, new Object[] {messageValue}, true, null);
    }

    public FieldRule(String fieldName,
                     String relationMsgKey,
                     Function<TObject, TField> fieldGetter,
                     Function<TField, Boolean> predicate,
                     String messageKey,
                     Object[] messageValues) {
        this(fieldName, relationMsgKey, fieldGetter, predicate, messageKey, messageValues, true, null);
    }

    public FieldRule(String fieldName,
                     String relationMsgKey,
                     Function<TObject, TField> fieldGetter,
                     Function<TField, Boolean> predicate,
                     String messageKey,
                     Object[] messageValues,
                     boolean skipNulls) {
        this(fieldName, relationMsgKey, fieldGetter, predicate, messageKey, messageValues,skipNulls, null);
    }

    public FieldRule(String fieldName,
                     String relationMsgKey,
                     Function<TObject, TField> fieldGetter,
                     Function<TField, Boolean> predicate,
                     String messageKey,
                     Object[] messageValues,
                     boolean skipNulls,
                     Predicate<TObject> isActive) {
        Objects.requireNonNull(fieldGetter);
        Objects.requireNonNull(predicate);

        this.fieldName = fieldName;
        this.relationMsgKey = relationMsgKey;
        this.fieldGetter = fieldGetter;
        this.predicate = predicate;
        this.messageKey = messageKey;
        this.messageValues = messageValues == null ? new Object[] {} : messageValues;
        this.skipNulls = skipNulls;
        this.isActive = isActive;
    }

    public Optional<FieldRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        if (object == null) {
            return Optional.empty();
        }

        if (isActive != null && !isActive.test(object)) {
            return Optional.empty();
        }

        TField fieldValue = fieldGetter.apply(object);
        if (skipNulls && fieldValue == null) {
            return Optional.empty();
        }

        Boolean result = predicate.apply(fieldValue);
        if (result == null || !result) {
            String message = messageSource.getMessage(messageKey, messageValues, messageKey, Locale.getDefault());

            return Optional.of(new FieldRuleViolation(fieldName, relationMsgKey, message));
        }

        return Optional.empty();
    }
}
