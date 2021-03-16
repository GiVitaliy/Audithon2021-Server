package ru.audithon.common.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanProcessingSubClass<TObject, TChild extends TObject> implements BeanProcessing<TObject> {

    private static final Logger logger = LoggerFactory.getLogger(BeanProcessingSubClass.class);

    private final Class<TChild> childClass;
    private final BeanProcessing<TChild> beanProcessing;

    BeanProcessingSubClass(Class<TChild> childClass, BeanProcessing<TChild> beanProcessing) {
        this.childClass = childClass;
        this.beanProcessing = beanProcessing;
    }

    public static <TObject, TChild extends TObject> BeanProcessingSubClassBuilder<TObject, TChild> builder() {
        return new BeanProcessingSubClassBuilder<>();
    }

    @Override
    public <T extends TObject> void process(T object) {
        if (!childClass.isAssignableFrom(object.getClass())) {
            logger.error("invalid object class: " + object.getClass()
                + " (" + childClass + ") required");
        }

        beanProcessing.process(childClass.cast(object));
    }
}
