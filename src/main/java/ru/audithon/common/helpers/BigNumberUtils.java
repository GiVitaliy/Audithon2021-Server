package ru.audithon.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BigNumberUtils {
    public static final BigDecimal ZERO_CLOSE_AMOUNT = new BigDecimal(0.000001);
    public static final BigDecimal HUGE_AMOUNT = BigDecimal.valueOf(Long.MAX_VALUE);

    private static final int DEFAULT_INTERNAL_SCALE = 15;

    private static int getInternalScale(int newScale) {
        return DEFAULT_INTERNAL_SCALE > newScale ? DEFAULT_INTERNAL_SCALE : newScale;
    }

    public static boolean areCloseEnough(BigDecimal amount1, BigDecimal amount2){
        if (amount1 == null && amount2 == null)
            return false;

        if ((amount1 != null && amount2 == null) || amount1 == null)
            return false;

        return amount1.subtract(amount2).abs().compareTo(ZERO_CLOSE_AMOUNT) <= 0;
    }

    public static boolean isCloseToZero(BigDecimal amount) {
        return amount != null && amount.abs().compareTo(ZERO_CLOSE_AMOUNT) <= 0;
    }

    public static String format(BigDecimal number, int minDecimalDigits, int maxDecimalDigits) {
        if (number == null) return "null";

        number = number.setScale(maxDecimalDigits, RoundingMode.HALF_UP);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(maxDecimalDigits);
        df.setMinimumFractionDigits(minDecimalDigits);
        df.setGroupingUsed(false);

        return df.format(number);
    }

    public static String format(BigDecimal number, int decimalDigits) {
        return format(number, decimalDigits, decimalDigits);
    }

    public static BigDecimal safelyAdd(BigDecimal value1, BigDecimal value2) {
        return coalesce(value1, BigDecimal.ZERO).add(coalesce(value2, BigDecimal.ZERO));
    }

    public static BigDecimal safelySubtract(BigDecimal minuend, BigDecimal subtrahend) {
        return coalesce(minuend, BigDecimal.ZERO).subtract(coalesce(subtrahend, BigDecimal.ZERO));
    }

    public static <T> T coalesce(T a, T b) {
        return a == null ? b : a;
    }

    public static BigDecimal sum(Collection<BigDecimal> values) {
        Objects.requireNonNull(values);

        return values.stream().map(Objects::requireNonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal average(Collection<BigDecimal> values, int scaleTo) {
        Objects.requireNonNull(values);

        if (values.size() == 0) throw new IllegalArgumentException();

        BigDecimal sum = sum(values).setScale(getInternalScale(scaleTo), BigDecimal.ROUND_HALF_UP);
        return sum.divide(BigDecimal.valueOf(values.size()), scaleTo, BigDecimal.ROUND_HALF_UP);
    }

    public static AverageFullResult averageFull(Collection<BigDecimal> values, int scaleTo) {
        Objects.requireNonNull(values);

        BigDecimal average = average(values, getInternalScale(scaleTo));
        BigDecimal dispersion = sum(values.stream().map(v -> (v.subtract(average)).pow(2) ).collect(Collectors.toList()))
                .divide(BigDecimal.valueOf(values.size()), getInternalScale(scaleTo), BigDecimal.ROUND_HALF_UP);
        BigDecimal standartDeviation = sqrt(dispersion, getInternalScale(scaleTo));

        return new AverageFullResult(
                average.setScale(scaleTo, RoundingMode.HALF_UP),
                dispersion.setScale(scaleTo, RoundingMode.HALF_UP),
                standartDeviation.setScale(scaleTo, RoundingMode.HALF_UP));
    }

    public static BigDecimal percentageOf(long nominator, long denominator, int scaleTo) {
        return percentageOf(BigDecimal.valueOf(nominator), BigDecimal.valueOf(denominator), scaleTo);
    }

    public static BigDecimal percentageOf(BigDecimal nominator, BigDecimal denominator, int scaleTo) {
        Objects.requireNonNull(nominator);
        Objects.requireNonNull(denominator);

        if (nominator.equals(BigDecimal.ZERO)) { return BigDecimal.ZERO; }
        if (denominator.equals(BigDecimal.ZERO)) {
            throw new IllegalArgumentException("Значение делителя не может быть нулевым");
        }

        return BigDecimal.valueOf(100).setScale(getInternalScale(scaleTo), RoundingMode.UNNECESSARY)
                .multiply(nominator)
                .divide(denominator, BigDecimal.ROUND_HALF_UP)
                .setScale(scaleTo, RoundingMode.HALF_UP);
    }

    public static BigDecimal sqrt(BigDecimal value, int scaleTo) {
        Objects.requireNonNull(value);

        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()));
        if (Objects.equals(x, BigDecimal.ZERO)) { return BigDecimal.ZERO; }

        return x.add(new BigDecimal(value.subtract(x.multiply(x)).doubleValue() / (x.doubleValue() * 2.0)))
                .setScale(scaleTo, RoundingMode.HALF_UP);
    }

    @AllArgsConstructor
    public static class AverageFullResult {
        public static final AverageFullResult ZERO = new AverageFullResult(BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO);

        @Getter
        private BigDecimal averageValue;
        @Getter
        private BigDecimal dispersion;
        @Getter
        private BigDecimal standartDeviation;
    }
}
