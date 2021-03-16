package ru.audithon.common.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import ru.audithon.egissostat.infrastructure.MessageTranslator;

@JsonSubTypes({
                  @JsonSubTypes.Type(value = BeanRuleViolation.class, name = "bean"),
                  @JsonSubTypes.Type(value = BeanRuleNavigatableViolation.class, name = "navigatable"),
                  @JsonSubTypes.Type(value = FieldRuleViolation.class, name = "field"),
                  @JsonSubTypes.Type(value = RelationRuleViolation.class, name = "relation"),
                  @JsonSubTypes.Type(value = CollectionRuleViolation.class, name = "collection"),
                  @JsonSubTypes.Type(value = CollectionItemRuleViolation.class, name = "collectionItem")
              })
public interface RuleViolation {
    String getViolationType();
    String getMessage();
    String getFinalUserMessage(MessageTranslator messageTranslator);
}
