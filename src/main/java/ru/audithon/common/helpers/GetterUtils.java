package ru.audithon.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GetterUtils {
    public static <T> Collection<T> getByPages(int limit, Function<PageCounters, Collection<T>> pageGetter, Predicate<T> filter) {
        List<T> results = new ArrayList<>();
        int startIndex = 0;

        while (true) {
            Collection<T> pageResults = pageGetter.apply(new PageCounters(startIndex, limit));

            //убираем элемнты, к которым нет доступа, пока не наберем нужного числа в результате
            results.addAll(pageResults.stream()
                    .filter(item -> results.size() < limit && filter.test(item))
                    .collect(Collectors.toList()));

            if (results.size() >= limit || pageResults.size() < limit) {
                break;
            } else {
                startIndex += pageResults.size();
            }
        }

        return results;
    }

    @AllArgsConstructor
    public static class PageCounters {
        @Getter
        private int startIndex;
        @Getter
        private int limit;
    }
}
