package ru.audithon.common.validation;

import com.google.common.base.Strings;

import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class Converter {
    public static <T> Set<RuleViolation> convert(Set<ConstraintViolation<T>> constraintViolations) {
        return constraintViolations.stream()
            .map(cv -> {
                if (cv.getPropertyPath() != null &&
                    !Strings.isNullOrEmpty(cv.getPropertyPath().toString())) {
                    return new FieldRuleViolation(cv.getPropertyPath().toString(),
                            cv.getLeafBean() !=  null ? cv.getLeafBean().toString() : null, cv.getMessage());
                }

                return new BeanRuleViolation(cv.getMessage());
            })
            .collect(Collectors.toSet());
    }
}
