package ru.audithon.common.bl;

import ru.audithon.common.validation.RuleViolation;

import java.util.Set;

public interface ObjectValidator<T> {
    boolean validate(T object, Set<RuleViolation> ruleViolations);
}
