package ru.audithon.common.validation;

import com.google.common.collect.ImmutableList;
import org.springframework.context.MessageSource;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BeanValidationBase<TObject> implements BeanValidation<TObject> {
    private final ImmutableList<Rule<TObject>> rules;
    private final MessageSource messageSource;

    BeanValidationBase(List<? extends Rule<TObject>> beanRules, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");
        this.rules = ImmutableList.copyOf(beanRules);
        this.messageSource = messageSource;
    }

    public static <T> BeanValidationBuilder<T> builder(MessageSource messageSource) {
        return new BeanValidationBuilder<>(messageSource);
    }

    @Override
    public <T extends TObject> Set<RuleViolation> validate(T object) {
        Set<RuleViolation> ruleViolations = new HashSet<>();

        if (object != null) {
            rules.forEach(fr -> {
                fr.validate(object, messageSource).ifPresent(ruleViolations::add);
            });
        }

        return ruleViolations;
    }
}
