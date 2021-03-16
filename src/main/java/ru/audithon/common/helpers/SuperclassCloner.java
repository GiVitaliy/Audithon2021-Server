package ru.audithon.common.helpers;

import com.rits.cloning.Cloner;
import com.rits.cloning.IInstantiationStrategy;
import com.rits.cloning.ObjenesisInstantiationStrategy;

public class SuperclassCloner <T extends S, S> {
    private final Class<S> superType;
    private final Class<T> childType;
    private final Cloner cloner = new Cloner(new InstantiationStrategy());

    public SuperclassCloner(Class<T> childType, Class<S> superType) {
        this.childType = childType;
        this.superType = superType;
    }

    @SuppressWarnings("unchecked")
    public T clone(S object){
        return (T)cloner.deepClone(object);
    }

    @SuppressWarnings("unchecked")
    class InstantiationStrategy implements IInstantiationStrategy {
        @Override
        public <U> U newInstance(Class<U> c) {
            ObjenesisInstantiationStrategy standardStrategy = ObjenesisInstantiationStrategy.getInstance();
            if(c.equals(superType) || superType.isAssignableFrom(c)) {
                return (U)standardStrategy.newInstance(childType);
            }
            return standardStrategy.newInstance(c);
        }
    }
}
