package ru.audithon.egissostat.resources;

import ru.audithon.common.notification.NotificationMessage;
import ru.audithon.common.notification.NotificationSeverity;
import ru.audithon.common.validation.FieldRuleViolation;
import ru.audithon.common.validation.RuleViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationResultConverter {
    public static ApiResultDto toErrorsDto(String message, Set<RuleViolation> ruleViolations) {
        List<FieldErrorDto> errors = new ArrayList<>();
        ruleViolations.forEach(rv -> {
            if (rv instanceof FieldRuleViolation) {
                FieldRuleViolation rvf = (FieldRuleViolation)rv;
                errors.add(new FieldErrorDto(rvf.getFieldName(), rvf.getMessage()));
            } else {
                errors.add(new FieldErrorDto(null, rv.getMessage()));
            }
        });

        List<NotificationMessage> messages = new ArrayList<NotificationMessage>();
        messages.add(new NotificationMessage(NotificationSeverity.Error, message));

        return new ApiResultDto(messages, errors);
    }
}
