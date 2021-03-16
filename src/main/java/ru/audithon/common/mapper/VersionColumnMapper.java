package ru.audithon.common.mapper;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VersionColumnMapper<TObject, TField> extends ColumnMapper<TObject, TField> {

    private VersionColumnMapper(Class<TField> fieldClass,
                                String columnName,
                                Function<TObject, TField> getter,
                                BiConsumer<TObject, TField> setter) {
        super(fieldClass, columnName, getter, setter, true, true, false);
    }

    @SuppressWarnings("unchecked")
    public TField setNextVersion(TObject object) {
        if (LocalDateTime.class.equals(this.fieldClass)) {
            LocalDateTime oldValue =  (LocalDateTime)getter.apply(object);
            LocalDateTime newValue = LocalDateTime.now();;
            this.setter.accept(object, (TField)newValue);

            return (TField)oldValue;
        }

        if (Integer.class.equals(this.fieldClass)) {
            Integer oldValue =  (Integer)getter.apply(object);
            if (oldValue == null ) {
                oldValue = 0;
            }
            Integer newValue = oldValue + 1;
            this.setter.accept(object, (TField)newValue);

            return (TField)oldValue;
        }

        if (Long.class.equals(this.fieldClass)) {
            Long oldValue =  (Long)getter.apply(object);
            if (oldValue == null ) {
                oldValue = 0L;
            }
            Long newValue = oldValue + 1;
            this.setter.accept(object, (TField)newValue);

            return (TField)oldValue;
        }

        throw new MapperException("Unsupported version field type");

    }

    @SuppressWarnings("unchecked")
    public void setOldVersion(TObject object, Object version) {
        this.setter.accept(object, (TField)version);
    }

    public static <TObject> VersionColumnMapper<TObject, LocalDateTime> timestamp(
        String columnName,
        Function<TObject, LocalDateTime> getter,
        BiConsumer<TObject, LocalDateTime> setter)
    {
        return new VersionColumnMapper<>(LocalDateTime.class, columnName, getter, setter);
    }

    public static <TObject> VersionColumnMapper<TObject, Integer> incrementalInt(
        String columnName,
        Function<TObject, Integer> getter,
        BiConsumer<TObject, Integer> setter)
    {
        return new VersionColumnMapper<>(Integer.class, columnName, getter, setter);
    }

    public static <TObject> VersionColumnMapper<TObject, Long> incrementalLong(
        String columnName,
        Function<TObject, Long> getter,
        BiConsumer<TObject, Long> setter)
    {
        return new VersionColumnMapper<>(Long.class, columnName, getter, setter);
    }
}
