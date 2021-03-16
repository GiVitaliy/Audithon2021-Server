package ru.audithon.common.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class BeanCondition<TObject> implements BeanValidation<TObject> {
    private final Predicate<TObject> condition;
    private final BeanValidation<TObject> onTrue;
    private BeanValidation<TObject> onFalse;

    private BeanCondition(Predicate<TObject> condition, BeanValidation<TObject> onTrue) {
        this.condition = condition;
        this.onTrue = onTrue;
    }


    public BeanCondition<TObject> orElse(BeanValidation<TObject> onFalse) {
        this.onFalse = onFalse;
        return this;
    }

    public static <TObject> BeanCondition<TObject> when(Predicate<TObject> condition, BeanValidation<TObject> onTrue) {
        return new BeanCondition<>(condition, onTrue);
    }

    @Override
    public <T extends TObject> Set<RuleViolation> validate(T object) {
        Set<RuleViolation> ruleViolations = new HashSet<>();

        if (object != null) {
            if (condition.test(object)) {
                return onTrue.validate(object);
            } else if (onFalse != null) {
                return onFalse.validate(object);
            }
        }

        return ruleViolations;
    }

}
