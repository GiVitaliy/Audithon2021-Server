package ru.audithon.common.mapper;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class KeyColumnMapper<TObject, TKey, TField> extends ColumnMapper<TObject, TField> {
    @Getter private final Function<TKey, TField> keyGetter;

    private KeyColumnMapper(Class<TField> fieldClass, String columnName,
                              Function<TObject, TField> getter,
                              BiConsumer<TObject, TField> setter,
                              Function<TKey, TField> keyGetter,
                              boolean insertable,
                              boolean updatable,
                              boolean generated) {
        super(fieldClass, columnName, getter, setter, insertable, updatable, generated);

        this.keyGetter = keyGetter;
    }

    public static <TObject, TKey, TField> KeyColumnMapper<TObject, TKey, TField> of(
        Class<TField> fieldClass,
        String columnName,
        Function<TObject, TField> getter,
        BiConsumer<TObject, TField> setter,
        Function<TKey, TField> keyGetter)
    {
        return new KeyColumnMapper<>(fieldClass, columnName, getter, setter, keyGetter,
            true, true, false);
    }

    public static <TObject, TKey, TField> KeyColumnMapper<TObject, TKey, TField> of(
        Class<TField> fieldClass,
        String columnName,
        Function<TObject, TField> getter,
        BiConsumer<TObject, TField> setter,
        Function<TKey, TField> keyGetter,
        boolean insertable,
        boolean updatable)
    {
        return new KeyColumnMapper<>(fieldClass, columnName, getter, setter, keyGetter,
            insertable, updatable, false);
    }

    public static <TObject, TKey, TField> KeyColumnMapper<TObject, TKey, TField> of(
        Class<TField> fieldClass,
        String columnName,
        Function<TObject, TField> getter,
        BiConsumer<TObject, TField> setter,
        Function<TKey, TField> keyGetter,
        boolean generated)
    {
        return new KeyColumnMapper<>(fieldClass, columnName, getter, setter, keyGetter,
            !generated, !generated, generated);
    }
}
