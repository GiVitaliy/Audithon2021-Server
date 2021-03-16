package ru.audithon.common.validation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

@Data
@RequiredArgsConstructor
public class BeanRuleNavigatableViolation implements RuleViolation {
    private final String violationType = "navigatable";
    private final String message;
    private final String navigateKey;
    private final RuleViolationNavigateKind navigateKind;
    private final String finalUserMessage;

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        return messageTranslator.translate(finalUserMessage, null);
    }
}
