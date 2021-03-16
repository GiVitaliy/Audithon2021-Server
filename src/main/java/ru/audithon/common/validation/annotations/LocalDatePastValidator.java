package ru.audithon.common.validation.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class LocalDatePastValidator implements ConstraintValidator<Past, LocalDate> {

    @Override
    public void initialize(Past constraintAnnotation) {
    }

    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.isBefore(LocalDate.now());
    }

}
