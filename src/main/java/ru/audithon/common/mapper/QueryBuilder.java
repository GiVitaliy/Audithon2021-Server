package ru.audithon.common.mapper;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class QueryBuilder {
    private List<QueryExpression> expressions = new ArrayList<>();

    public QueryBuilder add(QueryExpression qe) {
        Objects.requireNonNull(qe);
        expressions.add(qe);

        return this;
    }

    public QueryBuilder removeNullValues() {
        expressions.removeIf(qe -> qe.getValue() == null);
        return this;
    }

    public String getExpression() {
        return expressions.stream()
            .map(QueryExpression::getExpression)
            .collect(Collectors.joining(" and "));
    }

    public String getWhereExpression() {
        String expression = expressions.stream()
            .map(QueryExpression::getExpression)
            .collect(Collectors.joining(" and "));

        if (Strings.isNullOrEmpty(expression)) {
            return "";
        }

        return " where " + expression;
    }

    public Object[] getValues() {
        return expressions.stream()
            .map(QueryExpression::getValue)
            .collect(Collectors.toList())
            .toArray();
    }
}
