package ru.audithon.common.bl;

import ru.audithon.common.processing.BeanProcessing;

public class ObjectProcessorBase<T> implements ObjectProcessor<T> {

    private final BeanProcessing<T> beanProcessing;

    public ObjectProcessorBase(BeanProcessing<T> beanProcessing) {
        this.beanProcessing = beanProcessing;
    }

    public BeanProcessing<T> getBeanProcessing() {
        return beanProcessing;
    }

    @Override
    public void process(T object) {
        process(beanProcessing, object);
    }

    public static <T> void process(BeanProcessing<T> beanProcessing, T object) {
        if (beanProcessing == null) {
            return;
        }

        beanProcessing.process(object);
    }
}
