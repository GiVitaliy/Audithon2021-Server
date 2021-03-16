package ru.audithon.common.bl;

import ru.audithon.common.validation.BeanValidation;
import ru.audithon.common.validation.RuleViolation;

import java.util.Objects;
import java.util.Set;

public class ObjectValidatorBase<T> implements ObjectValidator<T> {

    private final BeanValidation<T> beanValidation;

    public ObjectValidatorBase(BeanValidation<T> beanValidation) {
        this.beanValidation = beanValidation;
    }

    public BeanValidation<T> getBeanValidation() {
        return beanValidation;
    }

    @Override
    public boolean validate(T object, Set<RuleViolation> ruleViolations) {
        return validate(beanValidation, object, ruleViolations);
    }

    public static <T> boolean validate(BeanValidation<T> beanValidation,
                                       T object, Set<RuleViolation> ruleViolations) {
        Objects.requireNonNull(ruleViolations, "ruleViolations is null");

        if (beanValidation == null) {
            return true;
        }

        Set<RuleViolation> result = beanValidation.validate(object);
        if (result.size() > 0) {
            ruleViolations.addAll(result);
            return false;
        }

        return true;
    }
}
