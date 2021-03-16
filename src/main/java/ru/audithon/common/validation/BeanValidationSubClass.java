package ru.audithon.common.validation;

import com.google.common.collect.Sets;
import org.springframework.context.MessageSource;

import java.util.Set;

public class BeanValidationSubClass<TObject, TChild extends TObject> implements BeanValidation<TObject> {

    private final Class<TChild> childClass;
    private final BeanValidation<TChild> beanValidation;

    BeanValidationSubClass(Class<TChild> childClass, BeanValidation<TChild> beanValidation) {
        this.childClass = childClass;
        this.beanValidation = beanValidation;
    }

    public static <TObject, TChild extends TObject> BeanValidationSubClassBuilder<TObject, TChild> builder(
        MessageSource messageSource) {
        return new BeanValidationSubClassBuilder<>(messageSource);
    }

    public <T extends TObject> Set<RuleViolation> validate(T object) {
        if (!childClass.isAssignableFrom(object.getClass())) {
            return Sets.newHashSet(
                new BeanRuleViolation("invalid object class: " + object.getClass()
                    + " (" + childClass + ") required"));
        }

        return beanValidation.validate(childClass.cast(object));
    }
}
