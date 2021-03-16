package ru.audithon.common.validation;

import lombok.Data;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RelationRuleViolation implements RuleViolation {
    private final String violationType = "relation";
    private final Set<RuleViolation> ruleViolations;
    private final String relationName;
    private final String finalUserRelationName;

    public RelationRuleViolation(String relationName, String finalUserRelationName, Set<RuleViolation> ruleViolations) {
        Objects.requireNonNull(ruleViolations, "ruleViolations is null");
        Objects.requireNonNull(relationName, "relationName is null");

        this.relationName = relationName;
        this.finalUserRelationName = finalUserRelationName;
        this.ruleViolations = ruleViolations;
    }

    @Override
    public String getMessage() {
        return ruleViolations.stream()
            .map(rv -> relationName + ": " + rv)
            .collect(Collectors.joining("\n"));
    }

    @Override
    public String getFinalUserMessage(MessageTranslator messageTranslator){
        return ruleViolations.stream()
                .map(rv -> messageTranslator.translate(
                            finalUserRelationName != null ? finalUserRelationName : relationName, null)
                        + ": " + rv.getFinalUserMessage(messageTranslator))
                .collect(Collectors.joining("\n"));
    }
}
