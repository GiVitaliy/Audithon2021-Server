package ru.audithon.common.validation.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class LocalDatePastOrCurrentValidator implements ConstraintValidator<PastOrCurrent, LocalDate> {

    @Override
    public void initialize(PastOrCurrent constraintAnnotation) {
    }

    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalDate now = LocalDate.now();
        return value.isBefore(now) || value.equals(now);
    }

}
