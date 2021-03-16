package ru.audithon.common.validation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

@Data
@RequiredArgsConstructor
public class BeanRuleViolation implements RuleViolation {
    private final String violationType = "bean";
    private final String message;

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        return messageTranslator.translate(message, null);
    }
}
