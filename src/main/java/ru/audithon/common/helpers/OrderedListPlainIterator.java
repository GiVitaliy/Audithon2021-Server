package ru.audithon.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class OrderedListPlainIterator<TSource, TIterated> implements Iterator<TIterated> {
    private final List<TSource> values;
    private final BiPredicate<TSource, TSource> sameGroupDeterminator;
    private final Function<List<TSource>, TIterated> iteratedItemSupplier;

    private TIterated currentObject;
    private int sourcePosition = 0;

    public OrderedListPlainIterator(List<TSource> values,
                             BiPredicate<TSource, TSource> sameGroupDeterminator,
                             Function<List<TSource>, TIterated> iteratedItemSupplier) {
        this.values = values;
        this.sameGroupDeterminator = sameGroupDeterminator;
        this.iteratedItemSupplier = iteratedItemSupplier;
    }

    @Override
    public boolean hasNext() {
        return sourcePosition < values.size() - 1;
    }

    @Override
    public TIterated next() {
        List<TSource> groupValues = new ArrayList<>();

        TSource prevValue = null;
        TSource thisValue = sourcePosition <= values.size() - 1 ? values.get(sourcePosition) : null;
        while (thisValue != null && (prevValue == null || sameGroupDeterminator.test(prevValue, thisValue))) {
            groupValues.add(thisValue);

            prevValue = thisValue;
            sourcePosition++;
            thisValue = sourcePosition <= values.size() - 1 ? values.get(sourcePosition) : null;
        }

        if (groupValues.size() > 0) {
            currentObject = this.iteratedItemSupplier.apply(groupValues);
            return currentObject;
        }

        currentObject = null;
        return null;
    }

    public Progress getProgress() {
        return new Progress(sourcePosition + 1, values.size());
    }

    @AllArgsConstructor
    public static class Progress {
        @Getter
        private int current;
        @Getter
        private int total;
    }
}
