package ru.audithon.common.processing;

import java.util.function.Consumer;

public class BeanProcessor<TObject> implements Processor<TObject>{
    private final Consumer<TObject> processor;

    public BeanProcessor(Consumer<TObject> processor) {
        this.processor = processor;
    }

    public void process(TObject object) {
        processor.accept(object);
    }
}
