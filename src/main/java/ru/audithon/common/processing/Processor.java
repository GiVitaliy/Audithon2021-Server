package ru.audithon.common.processing;

public interface Processor<TObject>  {
    void process(TObject object);
}
