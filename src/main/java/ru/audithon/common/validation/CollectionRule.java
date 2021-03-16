package ru.audithon.common.validation;

import org.springframework.context.MessageSource;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionRule<TObject, TChild> implements Rule<TObject> {
    private final String fieldName;
    private final String relationMsgKey;
    private final BeanValidation<TChild> beanValidation;
    private final Function<TObject, Collection<TChild>> relationCollectionGetter;

    public CollectionRule(String fieldName, String relationMsgKey,
                          Function<TObject, Collection<TChild>> relationCollectionGetter,
                          BeanValidation<TChild> beanValidation) {
        this.fieldName = fieldName;
        this.relationMsgKey = relationMsgKey;
        this.beanValidation = beanValidation;
        this.relationCollectionGetter = relationCollectionGetter;
    }

    public Optional<RuleViolation> validate(TObject object, MessageSource messageSource) {
        Objects.requireNonNull(messageSource, "Parameter messageSource can not be null");

        Collection<TChild> children = relationCollectionGetter.apply(object);
        if (children == null) {
            return Optional.empty();
        }

        Set<CollectionItemRuleViolation> violations = new HashSet<>();
        int index = 0;
        for(TChild child: children) {
            violations.add(new CollectionItemRuleViolation(index++, beanValidation.validate(child)));
        }

        if (violations.isEmpty() || violations.stream().noneMatch(v -> v.getRuleViolations().size() > 0)) {
            return Optional.empty();
        }

        return Optional.of(new CollectionRuleViolation(fieldName, relationMsgKey, "Коллекция содержит некорректные данные", violations));
    }

    public static <T> void validateCollection(Collection<T> items,
                                        String collectionFieldName, String relationName,
                                        BiConsumer<T, Set<RuleViolation>> itemValidator,
                                        Collection<RuleViolation> targetViolations) {
        Set<CollectionItemRuleViolation> collectionViolations = new HashSet<>();

        int index = 0;
        for(T item: items) {
            Set<RuleViolation> itemViolations = new HashSet<>();

            itemValidator.accept(item, itemViolations);

            if (!itemViolations.isEmpty()) {
                collectionViolations.add(new CollectionItemRuleViolation(index, itemViolations));
            }
            index++;
        }

        if (!collectionViolations.isEmpty()) {
            targetViolations.add(new CollectionRuleViolation(collectionFieldName, relationName,
                    collectionViolations));
        }
    }
}
