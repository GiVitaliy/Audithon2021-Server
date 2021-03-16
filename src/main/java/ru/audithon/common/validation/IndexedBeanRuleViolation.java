package ru.audithon.common.validation;

import lombok.Data;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

import java.util.List;
import java.util.Objects;

@Data
public class IndexedBeanRuleViolation implements RuleViolation {
    private final String violationType = "indexedBean";
    private final String message;
    private final List<Integer> ordinalIndexes;

    public IndexedBeanRuleViolation(List<Integer> ordinalIndexes, String message) {
        Objects.requireNonNull(ordinalIndexes, "ordinalIndexes is null");

        this.ordinalIndexes = ordinalIndexes;
        this.message = message;
    }

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        return messageTranslator.translate(message, null);
    }
}
