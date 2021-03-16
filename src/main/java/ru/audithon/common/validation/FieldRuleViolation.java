package ru.audithon.common.validation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.NoSuchMessageException;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

@Data
@RequiredArgsConstructor
public class FieldRuleViolation implements RuleViolation {
    private final String violationType = "field";
    private final String fieldName;
    private final String relationMsgKey;
    private final String message;

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        String finalUserFieldName;
        if (relationMsgKey == null || fieldName == null)
            finalUserFieldName = fieldName;
        else {
            String messageKey = String.format("%s.%s", relationMsgKey, fieldName);
            try {
                finalUserFieldName = messageTranslator.translate(messageKey, null);
            } catch(NoSuchMessageException ex){
                messageKey =String.format("%s.%s", relationMsgKey.toLowerCase(), fieldName);
                try {
                    finalUserFieldName = messageTranslator.translate(messageKey, null);
                } catch(NoSuchMessageException ex2) {
                    finalUserFieldName = fieldName;
                }
            }
        }

        return String.format("%s: %s", finalUserFieldName, message);
    }
}
