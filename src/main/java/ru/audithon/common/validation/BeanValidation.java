package ru.audithon.common.validation;

import java.util.Set;

public interface BeanValidation<TObject> {
    <T extends TObject> Set<RuleViolation> validate(T object);
}
