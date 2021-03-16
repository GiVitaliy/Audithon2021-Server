package ru.audithon.common.mapper;

import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class ColumnMapper<TObject, TField> {
    @Getter protected final String columnName;
    @Getter protected final BiConsumer<TObject, TField> setter;
    @Getter protected final Function<TObject, TField> getter;
    @Getter protected final Class<TField> fieldClass;

    @Getter protected final boolean insertable;
    @Getter protected final boolean updatable;
    @Getter protected final boolean generated;

    protected ColumnMapper(Class<TField> fieldClass, String columnName,
                           Function<TObject, TField> getter,
                           BiConsumer<TObject, TField> setter,
                           boolean insertable,
                           boolean updatable,
                           boolean generated) {
        this.columnName = columnName.trim();
        this.setter = setter;
        this.getter = getter;
        this.fieldClass = fieldClass;
        this.insertable = insertable;
        this.updatable = updatable;
        this.generated = generated;
    }

    public void copyFields(TObject from, TObject to) {
        setter.accept(to, getter.apply(from));
    }

    public static <TObject, TField> ColumnMapper<TObject, TField> of(Class<TField> fieldClass,
                                                                     String columnName,
                                                                     Function<TObject, TField> getter,
                                                                     BiConsumer<TObject, TField> setter) {
        return new ColumnMapper<>(fieldClass, columnName, getter, setter,
            true, true, false);
    }

    public static <TObject, TField> ColumnMapper<TObject, TField> of(Class<TField> fieldClass,
                                                                     String columnName,
                                                                     Function<TObject, TField> getter,
                                                                     BiConsumer<TObject, TField> setter,
                                                                     boolean insertable,
                                                                     boolean updatable) {
        return new ColumnMapper<>(fieldClass, columnName, getter, setter,
            insertable, updatable, false);
    }

    public static <TObject, TField> ColumnMapper<TObject, TField> of(Class<TField> fieldClass,
                                                                     String columnName,
                                                                     Function<TObject, TField> getter,
                                                                     BiConsumer<TObject, TField> setter,
                                                                     boolean generated) {
        return new ColumnMapper<>(fieldClass, columnName, getter, setter,
            !generated, !generated, generated);
    }
}
