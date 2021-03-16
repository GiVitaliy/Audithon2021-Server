package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.Optional;

public interface Rule<TObject> {
    Optional<? extends RuleViolation> validate(TObject object, MessageSource messageSource);
}
