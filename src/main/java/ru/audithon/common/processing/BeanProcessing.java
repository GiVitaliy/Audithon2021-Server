package ru.audithon.common.processing;

public interface BeanProcessing<TObject> {
    <T extends TObject> void process(T object);
}
