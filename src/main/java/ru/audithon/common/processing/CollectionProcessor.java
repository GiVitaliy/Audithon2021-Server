package ru.audithon.common.processing;

import java.util.Collection;
import java.util.function.Function;

public class CollectionProcessor<TObject, TChild> implements Processor<TObject> {
    private final BeanProcessing<TChild> beanProcessing;
    private final Function<TObject, Collection<TChild>> collectionGetter;

    public CollectionProcessor(Function<TObject, Collection<TChild>> collectionGetter, BeanProcessing<TChild> beanProcessing) {
        this.beanProcessing = beanProcessing;
        this.collectionGetter = collectionGetter;
    }

    @Override
    public void process(TObject object) {
        Collection<TChild> child = collectionGetter.apply(object);
        if (child == null) {
            return;
        }

        child.forEach(beanProcessing::process);
    }
}
