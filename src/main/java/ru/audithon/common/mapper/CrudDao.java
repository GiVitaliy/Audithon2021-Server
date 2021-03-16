package ru.audithon.common.mapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public interface CrudDao<TTable, TKey> {
    List<TTable> all();
    // HashMap<Integer, TTable> getAllHash(Function<TTable, Integer> getKey);
    <THKey> HashMap<THKey, TTable> getAllHash(Function<TTable, THKey> getKey);
    Optional<TTable> byId(TKey key);
    TTable byIdStrong(TKey key);
    TTable byIdStashed(TKey key);
    Optional<TTable> byId(TKey key, boolean updLock);
    List<TTable> byIds(Collection<TKey> keys);

    TTable insert(TTable value);
    int update(TTable value, TKey where);
    int delete(TKey key);

    /**
     * Обновляет всю коллекцию элементов, сравнивая ее с сущствующей для определения новых, измененных элементов и элементов, подлежащих удалению
     * @param items Элементы в актуальном состоянии
     * @param existing Элементы в БД
     * @param keyGetter Функция-получатель ключа элемента коллекции
     * @param itemChangeDetector Опциональный определитель наличия изменений в элементе (в сравнении с существующим в БД).
     *                           При отсутствии переданного значения считается, что все существующие элементы подлежат обновлению (кроме удаляемых).
     * @return Список актуальных элементов с изменениями по результатам обработки.
     */
    List<TTable> syncCollection(List<TTable> items, List<TTable> existing, Function<TTable, TKey> keyGetter,
                                BiPredicate<TTable, TTable> itemChangeDetector);

    void cleanupStash();
}
