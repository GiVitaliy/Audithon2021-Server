package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class BeanValidationSubClassBuilder<TObject, TChild extends TObject> {
    private final List<Rule<TChild>> rules = new ArrayList<>();
    private final MessageSource messageSource;

    BeanValidationSubClassBuilder(MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        this.messageSource = messageSource;
    }

    public BeanValidationSubClassBuilder<TObject, TChild> add(Rule<TChild> rule) {
        Objects.requireNonNull(rule, "rule is null");
        rules.add(rule);
        return this;
    }

    public BeanValidationSubClassBuilder<TObject, TChild> add(List<? extends Rule<TChild>> ruleList) {
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

    public BeanValidationSubClass<TObject, TChild> build(Class<TChild> cls) {
        return new BeanValidationSubClass<>(cls,
            new BeanValidationBase<>(rules, messageSource));
    }
}
