package ru.audithon.common.types;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

@Data
@AllArgsConstructor
public class DatePeriod<T> {
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
    private T payload;

    public DatePeriod(LocalDate dateFrom, LocalDate dateToExcluded, T payload) {
        this(LocalDateTime.of(dateFrom, LocalTime.MIN), LocalDateTime.of(dateToExcluded, LocalTime.MIN), payload);
    }

    public boolean isEmptyOrNegative() {
        return !dateTo.isAfter(dateFrom);
    }

    public long getTotalDays() {
        return Math.max(0, Duration.between(dateFrom, dateTo).toDays());
    }

    public long getTotalMinutes() {
        return Math.max(0, Duration.between(dateFrom, dateTo).toMinutes());
    }

    public long getTotalHours() {
        return Math.max(0, Duration.between(dateFrom, dateTo).toHours());
    }

    public boolean includes(LocalDate date) {
        return includes(LocalDateTime.of(date, LocalTime.MIN));
    }
    public boolean includes(LocalDateTime date) {
        return !date.isBefore(dateFrom) && date.isBefore(dateTo);
    }

    public static <T> DatePeriod<T> ofIntersection(DatePeriod<T> period,
                                            LocalDateTime dateFromInclusive,
                                            LocalDateTime dateToExclusive) {
        return new DatePeriod<>(
            period.dateFrom.isBefore(dateFromInclusive)? dateFromInclusive: period.dateFrom,
            period.dateTo.isAfter(dateToExclusive)? dateToExclusive: period.dateTo,
            period.payload);
    }

    public static <T> DatePeriod<T> ofCoverage(Collection<DatePeriod<T>> periods) {

        LocalDateTime minDate = LocalDateTime.MAX;
        LocalDateTime maxDate = LocalDateTime.MIN;

        for(DatePeriod<T> period: periods) {
            if (period.getDateFrom().isBefore(minDate)) {
                minDate = period.getDateFrom();
            }
            if (period.getDateTo().isAfter(maxDate)) {
                maxDate = period.getDateTo();
            }
        }

        return new DatePeriod<T>(minDate, maxDate, null);
    }
}
