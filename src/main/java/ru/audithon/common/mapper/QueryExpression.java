package ru.audithon.common.mapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryExpression {
    private final String field;
    private final String operation;
    private final Object value;

    public String getExpression() {
        Objects.requireNonNull(field);
        Objects.requireNonNull(operation);
        Objects.requireNonNull(value);

        return " " + field + " " + operation + " ?";
    }

    public static QueryExpression eq(String field, Object value) {
        return new QueryExpression(field, "=", value);
    }

    public static QueryExpression neq(String field, Object value) {
        return new QueryExpression(field, "!=", value);
    }

    public static QueryExpression gt(String field, Object value) {
        return new QueryExpression(field, ">", value);
    }

    public static QueryExpression get(String field, Object value) {
        return new QueryExpression(field, ">=", value);
    }

    public static QueryExpression lt(String field, Object value) {
        return new QueryExpression(field, "<", value);
    }

    public static QueryExpression let(String field, Object value) {
        return new QueryExpression(field, "<=", value);
    }
}
