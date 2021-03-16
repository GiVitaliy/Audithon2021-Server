package ru.audithon.common.processing;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class BeanProcessingBase<TObject> implements BeanProcessing<TObject> {
    private final ImmutableList<Processor<TObject>> processors;

    BeanProcessingBase(List<? extends Processor<TObject>> beanProcessors) {
        this.processors = ImmutableList.copyOf(beanProcessors);
    }

    public static <T> BeanProcessingBuilder<T> builder() {
        return new  BeanProcessingBuilder<>();
    }

    @Override
    public <T extends TObject> void process(T object) {
        processors.forEach(proc -> {
            proc.process(object);
        });
    }

}

