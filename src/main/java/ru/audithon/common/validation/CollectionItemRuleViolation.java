package ru.audithon.common.validation;

import lombok.Data;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CollectionItemRuleViolation implements RuleViolation {
    private final String violationType = "collectionItem";
    private final Set<RuleViolation> ruleViolations;
    private final Integer itemIndex;

    public CollectionItemRuleViolation(Integer itemIndex, Set<RuleViolation> ruleViolations) {
        Objects.requireNonNull(ruleViolations, "ruleViolations is null");
        Objects.requireNonNull(itemIndex, "itemIndex is null");

        this.itemIndex = itemIndex;
        this.ruleViolations = ruleViolations;
    }

    @Override
    public String getMessage() {
        return String.format("Элемент %s коллекции содержит ошибки", itemIndex);
    }

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        return String.format("%s:\n%s", itemIndex, ruleViolations.stream()
                .map(rv -> rv.getFinalUserMessage(messageTranslator))
                .collect(Collectors.joining("\n")));
    }
}
