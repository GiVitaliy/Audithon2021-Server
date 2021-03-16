package ru.audithon.common.bl;

import ru.audithon.common.processing.BeanProcessing;
import ru.audithon.common.validation.BeanValidation;
import ru.audithon.common.validation.RuleViolation;

import java.util.Set;

public class ObjectLogicBase<T> implements ObjectValidator<T>, ObjectProcessor<T> {

    private final BeanValidation<T> beanValidation;
    private final BeanProcessing<T> beanProcessing;

    public ObjectLogicBase(BeanValidation<T> beanValidation,
                           BeanProcessing<T> beanProcessing) {
        this.beanValidation = beanValidation;
        this.beanProcessing = beanProcessing;
    }

    public BeanValidation<T> getBeanValidation() {
        return beanValidation;
    }

    public BeanProcessing<T> getBeanProcessing() {
        return beanProcessing;
    }

    @Override
    public boolean validate(T object, Set<RuleViolation> ruleViolations) {
        return ObjectValidatorBase.validate(beanValidation, object, ruleViolations);
    }

    @Override
    public void process(T object) {
        ObjectProcessorBase.process(beanProcessing, object);
    }
}
