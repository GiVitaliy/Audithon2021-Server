package ru.audithon.common.processing;

import java.util.function.Function;

public class RelationProcessor<TObject, TChild> implements Processor<TObject> {
    private final BeanProcessing<TChild> beanProcessing;
    private final Function<TObject, TChild> relationGetter;

    public RelationProcessor(Function<TObject, TChild> relationGetter, BeanProcessing<TChild> beanProcessing) {
        this.beanProcessing = beanProcessing;
        this.relationGetter = relationGetter;
    }

    @Override
    public void process(TObject object) {
        TChild child = relationGetter.apply(object);
        if (child == null) {
            return;
        }

        beanProcessing.process(child);
    }
}
