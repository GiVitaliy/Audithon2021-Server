package ru.audithon.common.mapper;

import java.sql.SQLException;

public interface SqlBiFunction<T, U, R> {
    R apply(T t,U u) throws SQLException;
}

