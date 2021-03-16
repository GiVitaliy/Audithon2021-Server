package ru.audithon.common.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeanProcessingSubClassBuilder<TObject, TChild extends TObject> {
    private final List<Processor<TChild>> processors = new ArrayList<>();

    BeanProcessingSubClassBuilder() {
    }

    public BeanProcessingSubClassBuilder<TObject, TChild> add(Processor<TChild> processor) {
        Objects.requireNonNull(processor, "processor is null");
        processors.add(processor);
        return this;
    }

    public BeanProcessingSubClassBuilder<TObject, TChild> add(List<? extends Processor<TChild>> processorList) {
        Objects.requireNonNull(processorList, "ruleList is null");
        if (processorList.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("processor is null");
        }

        processors.addAll(processorList);
        return this;
    }

    public BeanProcessingSubClass<TObject, TChild> build(Class<TChild> cls) {
        return
            new BeanProcessingSubClass<>(cls, new BeanProcessingBase<>(processors));
    }
}
