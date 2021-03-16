package ru.audithon.common.validation.annotations;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocalDatePastOrCurrentValidator.class)
public @interface PastOrCurrent {
    String message() default "{ru.audithon.common.validation.constraints.pastOrCurrent}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
