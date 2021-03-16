package ru.audithon.common.validation;

import lombok.Data;
import org.springframework.context.NoSuchMessageException;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CollectionRuleViolation implements RuleViolation {
    private final String violationType = "collection";
    private final Set<CollectionItemRuleViolation> ruleViolations;
    private final String fieldName;
    private final String relationMsgKey;
    private final String message;

    public CollectionRuleViolation(String fieldName, String relationMsgKey, String message, Set<CollectionItemRuleViolation> ruleViolations) {
        Objects.requireNonNull(ruleViolations, "ruleViolations is null");
        Objects.requireNonNull(fieldName, "fieldName is null");

        this.fieldName = fieldName;
        this.relationMsgKey = relationMsgKey;
        this.ruleViolations = ruleViolations;
        this.message = message;
    }

    public CollectionRuleViolation(String fieldName, String relationMsgKey,Set<CollectionItemRuleViolation> ruleViolations) {
        this(fieldName, relationMsgKey, "Некорректные данные в списке", ruleViolations);
    }

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

        String fieldCaption = finalUserFieldName;
        return String.format("%s: %s.\n%s", fieldCaption, message,
                ruleViolations.stream()
                .map(rv -> rv.getFinalUserMessage(messageTranslator))
                .collect(Collectors.joining("\n")));
    }
}
