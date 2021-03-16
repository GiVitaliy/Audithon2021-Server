package ru.audithon.common.validation;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RuleViolationNavigatableData<TKey> extends RuleViolationData {

    private TKey navigateKey;
    private RuleViolationNavigateKind navigateKind;

    public RuleViolationNavigatableData(String messageKey, Object[] messageValues,
                                        TKey navigateKey, RuleViolationNavigateKind navigateKind) {
        super(messageKey, messageValues);
        this.navigateKey = navigateKey;
        this.navigateKind = navigateKind;
    }

    public RuleViolationNavigatableData(String messageKey, Object messageValue,
                                        TKey navigateKey, RuleViolationNavigateKind navigateKind) {
        this(messageKey, new Object[]{messageValue}, navigateKey, navigateKind);
    }

}
