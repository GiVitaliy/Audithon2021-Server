package ru.audithon.common.validation;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class FieldConstraints {

    public static <T> NamedFunction<T, Boolean> notNull() {
        return new NamedFunctionImpl<>(Objects::nonNull,
            "a18main.validation.fieldConstraints.notNull",
            false);
    }

    public static NamedFunction<String, Boolean> notEmpty() {
        return new NamedFunctionImpl<>(v -> v != null && (v.trim().length() > 0),
            "a18main.validation.fieldConstraints.notEmpty",
            false);
    }

    public static NamedFunction<String, Boolean> lengthLessThen(int value) {
        return new NamedFunctionImpl<>(v -> v == null || v.length() < value,
            "a18main.validation.fieldConstraints.lengthLessThen",
            new Object[] {value});
    }

    public static NamedFunction<String, Boolean> lengthBetween(int low, int high) {
        return new NamedFunctionImpl<>(v -> v == null || (v.length() >= low && v.length() <= high),
            "a18main.validation.fieldConstraints.lengthBetween",
            new Object[] {low, high});
    }

    public static NamedFunction<String, Boolean> cyrillicLettersOnly() {
        return new NamedFunctionImpl<>(v -> v.matches("[а-яёА-ЯЁ]*"),
            "a18main.validation.fieldConstraints.cyrillicLettersOnly");
    }

    public static NamedFunction<String, Boolean> russianName() {
        return new NamedFunctionImpl<>(v -> v.matches("[а-яёА-ЯЁ\\-\\p{Space}]*"),
            "a18main.validation.fieldConstraints.russianName");
    }

    public static NamedFunction<String, Boolean> digitLiteralsOnly() {
        return new NamedFunctionImpl<>(v -> v.matches("[0-9]*"),
            "a18main.validation.fieldConstraints.digitLiteralsOnly");
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> lessThen(Comparable<T> value) {
        return lessThen(value, false);
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> lessThen(Comparable<T> value, boolean skipNull) {
        return new NamedFunctionImpl<>(v -> v != null ? value.compareTo(v) > 0 : skipNull,
                "a18main.validation.fieldConstraints.lessThen",
                new Object[] {value});
    }

    public static <T> NamedFunction<T, Boolean> equalTo(T value) {
        return new NamedFunctionImpl<>(v -> v.equals(v),
            "a18main.validation.fieldConstraints.equalTo",
            new Object[] {value});
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> greaterThen(Comparable<T> value) {
        return greaterThen(value, false);
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> greaterThen(Comparable<T> value, boolean skipNull) {
        return new NamedFunctionImpl<>(v -> v != null ? value.compareTo(v) < 0 : skipNull,
            "a18main.validation.fieldConstraints.greaterThen",
            new Object[] {value});
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> lessOrEqualThen(Comparable<T> value) {
        return lessOrEqualThen(value, false);
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> lessOrEqualThen(Comparable<T> value, boolean skipNull) {
        return new NamedFunctionImpl<>(v -> v != null ? value.compareTo(v) >= 0 : skipNull,
                "a18main.validation.fieldConstraints.lessOrEqualThen",
                new Object[] {value});
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> greaterOrEqualThen(Comparable<T> value) {
        return greaterOrEqualThen(value, false);
    }

    public static <T extends Comparable<T>> NamedFunction<T, Boolean> greaterOrEqualThen(Comparable<T> value, boolean skipNull) {
        return new NamedFunctionImpl<>(v -> v != null ? value.compareTo(v) <= 0 : skipNull,
                "a18main.validation.fieldConstraints.greaterOrEqualThen",
                new Object[] {value});
    }

    public static NamedFunction<LocalDate, Boolean> future() {
        return new NamedFunctionImpl<>(v -> LocalDate.now().isBefore(v),
            "a18main.validation.fieldConstraints.future");
    }

    public static NamedFunction<LocalDate, Boolean> notFuture() {
        return new NamedFunctionImpl<>(v -> !LocalDate.now().isBefore(v),
            "a18main.validation.fieldConstraints.notFuture");
    }

    public static NamedFunction<LocalDate, Boolean> notTooOldBirthDate() {
        return new NamedFunctionImpl<>(v -> v.getYear() >= 1900,
                "a18main.validation.fieldConstraints.notTooOldBirthDate");
    }

    public static NamedFunction<LocalDate, Boolean> notTooOld() {
        return new NamedFunctionImpl<>(v -> v.getYear() >= 1900,
            "a18main.validation.fieldConstraints.notTooOldBirthDate");
    }

    public static NamedFunction<LocalDate, Boolean> past() {
        return new NamedFunctionImpl<>(v -> v.isBefore(LocalDate.now()),
            "a18main.validation.fieldConstraints.past");
    }

    public static NamedFunction<LocalDate, Boolean> notPast() {
        return new NamedFunctionImpl<>(v -> !v.isBefore(LocalDate.now()),
            "a18main.validation.fieldConstraints.notPast");
    }
}

