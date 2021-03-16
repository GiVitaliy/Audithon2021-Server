package ru.audithon.common.mapper;

public interface PgCrudDao<TTable, TKey> extends CrudDao<TTable, TKey> {
    int nextSequenceValue();
}
