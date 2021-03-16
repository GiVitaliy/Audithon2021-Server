package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class RelationRule<TObject, TChild> implements Rule<TObject> {
    private final String relationName;
    private final BeanValidation<TChild> beanValidation;
    private final Function<TObject, TChild> relationGetter;

    public RelationRule(String relationName, Function<TObject, TChild> relationGetter, BeanValidation<TChild> beanValidation) {
        this.relationName = relationName;
        this.beanValidation = beanValidation;
        this.relationGetter = relationGetter;
    }

    public Optional<RelationRuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        TChild child = relationGetter.apply(object);
        if (child == null) {
            return Optional.empty();
        }

        Set<RuleViolation> violations = beanValidation.validate(child);
        if (violations.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new RelationRuleViolation(relationName,
                messageSource.getMessage(relationName, null, relationName, Locale.getDefault()),
                violations));
    }
}
