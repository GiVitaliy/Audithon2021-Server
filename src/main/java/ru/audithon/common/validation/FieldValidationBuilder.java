package ru.audithon.common.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FieldValidationBuilder<TObject, TField> {
    private final String fieldName;
    private final String relationMsgKey;
    private final Function<TObject, TField> fieldGetter;

    private final List<FieldRule<TObject, TField>> fieldRules = new ArrayList<>();

    protected FieldValidationBuilder(String fieldName, String relationMsgKey, Function<TObject, TField> fieldGetter) {
        this.fieldName = fieldName;
        this.relationMsgKey = relationMsgKey;
        this.fieldGetter = fieldGetter;
    }

    public FieldValidationBuilder<TObject, TField> add(Function<TField, Boolean> predicate, String messageKey) {
        return add(predicate, messageKey, null);
    }

    public FieldValidationBuilder<TObject, TField> add(Function<TField, Boolean> predicate,
                                                       String messageKey, Object value) {
        return add(predicate, messageKey, new Object[]{value});
    }

    public FieldValidationBuilder<TObject, TField> add(Function<TField, Boolean> predicate,
                                                       String messageKey, Object[] values) {
        fieldRules.add(new FieldRule<>(fieldName, relationMsgKey, fieldGetter, predicate, messageKey, values));

        return this;
    }

    @SafeVarargs
    public final FieldValidationBuilder<TObject, TField> add(
            NamedFunction<TField, Boolean> namedPredicate,
            NamedFunction<TField, Boolean>... namedPredicates) {
        return addConditional(null, namedPredicate, namedPredicates);
    }

    @SafeVarargs
    public final FieldValidationBuilder<TObject, TField> addConditional(
            Predicate<TObject> isActive,
            NamedFunction<TField, Boolean> namedPredicate,
            NamedFunction<TField, Boolean>... namedPredicates) {
        fieldRules.add(new FieldRule<>(fieldName, relationMsgKey, fieldGetter,
            namedPredicate, namedPredicate.getMessageKey(), namedPredicate.getValues(), namedPredicate.valueOnNull(), isActive));

        if (namedPredicates != null) {
            for (NamedFunction<TField, Boolean> f : namedPredicates) {
                fieldRules.add(new FieldRule<>(fieldName, relationMsgKey, fieldGetter,
                    f, f.getMessageKey(), f.getValues(), f.valueOnNull()));
            }
        }

        return this;
    }

    public List<FieldRule<TObject, TField>> build() {
        return fieldRules;
    }
}
