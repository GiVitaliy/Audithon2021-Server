package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class BeanValidationBuilder<T> {
    private final List<Rule<T>> rules = new ArrayList<>();
    private final MessageSource messageSource;

    BeanValidationBuilder(MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");
        this.messageSource = messageSource;
    }

    public BeanValidationBuilder<T> add(Rule<T> rule) {
        Objects.requireNonNull(rule, "rule is null");
        rules.add(rule);
        return this;
    }

    public BeanValidationBuilder<T> add(List<? extends Rule<T>> ruleList) {
        Objects.requireNonNull(ruleList, "ruleList is null");
        if (ruleList.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("rule is null");
        }

        rules.addAll(ruleList);
        return this;
    }

    public static <TObject, TField> FieldValidationBuilder<TObject, TField> forField(
        String fieldName, String relationMsgKey, Function<TObject, TField> fieldGetter) {
        return new FieldValidationBuilder<>(fieldName, relationMsgKey, fieldGetter);
    }

    public BeanValidation<T> build() {
        return new BeanValidationBase<>(rules, messageSource);
    }
}
