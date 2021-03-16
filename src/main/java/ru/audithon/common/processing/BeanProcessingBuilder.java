package ru.audithon.common.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BeanProcessingBuilder<T> {
    private final List<Processor<T>> processors = new ArrayList<>();

    BeanProcessingBuilder() {
    }

    public BeanProcessingBuilder<T> add(Processor<T> processor) {
        Objects.requireNonNull(processor, "processor is null");
        processors.add(processor);
        return this;
    }

    public BeanProcessingBuilder<T> add(List<? extends Processor<T>> processorList) {
        Objects.requireNonNull(processorList, "ruleList is null");
        if (processorList.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("processor is null");
        }

        processors.addAll(processorList);
        return this;
    }

    public BeanProcessing<T> build() {
        return new BeanProcessingBase<>(processors);
    }
}
