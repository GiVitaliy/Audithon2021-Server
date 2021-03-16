package ru.audithon.common.types;

import lombok.Getter;
import ru.audithon.common.helpers.DateUtils;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DateRange implements Cloneable {
    @Getter
    private final LocalDate dateFrom;
    @Getter
    private final LocalDate dateTo;
    @Getter
    private final boolean includesFrom;
    @Getter
    private final boolean includesTo;

    public final static DateRange DefaultEmptyRange = ofOpenRange(DateUtils.DefaultDate, DateUtils.DefaultDate);

    public DateRange(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, boolean includesFrom, boolean includesTo)
    {
        if (dateFrom == null)
            throw new IllegalArgumentException("Дата начала периода не может быть пустой");
        if (dateTo == null)
            throw new IllegalArgumentException("Дата окончания периода не может быть пустой");

        if (dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException(String.format("Некорректный интервал дат: %s - %s",
                    DateUtils.formatRuDate(dateFrom), DateUtils.formatRuDate(dateTo)));
        }

        this.dateFrom = dateFrom;
        this.dateTo = dateTo;

        this.includesFrom = includesFrom;
        this.includesTo = includesTo;
    }

    public DateRange(@NotNull DateRange source)
    {
        this(source.dateFrom, source.dateTo, source.includesFrom, source.includesTo);
    }

    /**
        Создает включающий обе даты интервал
    */
    public static DateRange ofClosedRange(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo)
    {
        return new DateRange(dateFrom, dateTo, true, true);
    }

    public static DateRange ofFromToMaxRight(@NotNull LocalDate dateFrom)
    {
        return new DateRange(dateFrom, DateUtils.MaxDate, true, true);
    }

    /**
     Создает включающий обе даты интервал
     */
    public static DateRange ofClosedRange(@NotNull DateRange source)
    {
        Objects.requireNonNull(source,"source is null");

        return new DateRange(
                source.includesFrom ? source.dateFrom : source.dateFrom.plusDays(1),
                source.includesTo? source.dateTo : source.dateTo.minusDays(1),
                true, true);
    }

    /**
     Создает интервал, не включающий обе даты
     */
    public static DateRange ofOpenRange(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo)
    {
        return new DateRange(dateFrom, dateTo, false, false);
    }

    /**
     Создает интервал, не включающий дату начала
     */
    public static DateRange ofLeftOpenRange(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo)
    {
        return new DateRange(dateFrom, dateTo, false, true);
    }

    /**
     Создает интервал, не включающий дату окончания
     */
    public static DateRange ofRightOpenRange(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo)
    {
        return new DateRange(dateFrom, dateTo, true, false);
    }

    public DateRange cloneWithNewFrom(@NotNull LocalDate dateFrom)
    {
        return new DateRange(dateFrom, this.getDateTo(), this.includesFrom, this.includesTo);
    }

    public DateRange cloneWithNewTo(@NotNull LocalDate dateTo)
    {
        return new DateRange(this.getDateFrom(), dateTo, this.includesFrom, this.includesTo);
    }

    public static DateRange ofDateMonth(LocalDate date) {
        Objects.requireNonNull(date);

        return ofClosedRange(DateUtils.startOfTheMonth(date), DateUtils.endOfTheMonth(date));
    }

    public static DateRange ofDateYear(LocalDate date) {
        Objects.requireNonNull(date);

        return ofClosedRange(DateUtils.startOfTheYear(date), DateUtils.endOfTheYear(date));
    }

    public static DateRange ofYear(int year) {
        return ofClosedRange(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    }

    /**
     Создает интервал в один день
     */
    public static DateRange ofOneDay(@NotNull LocalDate date)
    {
        return new DateRange(date, date, true, true);
    }

    /**
     * Возвращает список, в котором все пересекащиеся периоды из исходного объединены между собой
     * @param periods периоды с возможными пересечениями между собой
     * @return Список, в которм нет пересекающихся периодов
     */
    public static List<DateRange> mergeIntersectedPeriods(List<DateRange> periods) {
        List<DateRange> mergedPeriods = new ArrayList<>(periods);

        for(int index = 0; index < mergedPeriods.size() - 1; index++) {
            DateRange mergedPeriod = mergedPeriods.get(index);

            for(int index2 = index + 1; index2 < mergedPeriods.size(); index2++) {
                DateRange period2 = mergedPeriods.get(index2);

                if (mergedPeriod == period2) continue;

                if (mergedPeriod.intersectsWith(period2)) {
                    mergedPeriod = DateRange.ofClosedRange(
                            DateUtils.min(mergedPeriod.getMinDate(), period2.getMinDate()),
                            DateUtils.max(mergedPeriod.getMaxDate(), period2.getMaxDate()));
                    mergedPeriods.set(index2, mergedPeriod);
                }
            }

            mergedPeriods.set(index, mergedPeriod);
        }

        return mergedPeriods.stream().distinct().collect(Collectors.toList());
    }

    public LocalDate getMinDate()
    {
        if (isEmpty()) {
            throw new IllegalArgumentException(String.format("Нельзя получить минимальную дату у пустого периода %s", this));
        }

        return includesFrom ? dateFrom : dateFrom.plusDays(1);
    }

    public LocalDate getMaxDate()
    {
        if (isEmpty()) {
            throw new IllegalArgumentException(String.format("Нельзя получить максимальную дату у пустого периода %s", this));
        }

        return includesTo ? dateTo : dateTo.minusDays(1);
    }

    public LocalDate getMaxDateExcluded()
    {
        if (isEmpty()) {
            throw new IllegalArgumentException(String.format("Нельзя получить исключенную максимальную дату у пустого периода %s", this));
        }

        return includesTo && dateTo != LocalDate.MAX ? dateTo.plusDays(1) : dateTo;
    }

    public boolean startsOn(LocalDate date)
    {
        return getMinDate().equals(date);
    }

    public boolean endsOn(LocalDate date)
    {
        return getMaxDate().equals(date);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dateFrom, dateTo, includesFrom, includesTo);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o.getClass() != getClass())
            return false;
        DateRange e = (DateRange) o;

        if ((e.isEmpty() && !this.isEmpty() || (!e.isEmpty() && this.isEmpty())))
            return false;

        if (e.isEmpty() && this.isEmpty()) {
            return e.getDateFrom().equals(this.getDateFrom()) && e.getDateTo().equals(this.getDateTo());
        }

        return e.getMinDate().equals(this.getMinDate())
                && e.getMaxDate().equals(this.getMaxDate());
    }

    @Override
    public Object clone() {
        return new DateRange(this);
    }

    /**
     Пустым считается интервал без длины: границы совпадают и при этом не включены (обе или хотя бы одна)
     */
    public boolean isEmpty()
    {
        return (dateFrom.equals(dateTo) && !(includesFrom && includesTo))
                ||(!includesFrom && !includesTo && dateFrom.plusDays(1).equals(dateTo));
    }

    public boolean isOneDay()
    {
        return !isEmpty() && getMinDate().equals(getMaxDate());
    }

    public DateRange getUnion(@NotNull DateRange targetRange) {
        Objects.requireNonNull(targetRange,"targetRange is null");

        if (targetRange.isEmpty()) {
            return this;
        }

        if (this.isEmpty()) {
            return targetRange;
        }

        LocalDate newMaxDate = DateUtils.max(this.getMaxDate(), targetRange.getMaxDate());
        LocalDate newMinDate = DateUtils.min(this.getMinDate(), targetRange.getMinDate());

        return DateRange.ofClosedRange(newMinDate, newMaxDate);
    }

    public DateRange getIntersection(@NotNull DateRange targetRange)
    {
        Objects.requireNonNull(targetRange,"targetRange is null");

        if(targetRange.isEmpty() || this.isEmpty())
            return (DateRange)DefaultEmptyRange.clone();

        if(this.equals(targetRange))
            return (DateRange)this.clone();

        if(this.dateFrom.isAfter(targetRange.dateTo)
                || (this.dateFrom.equals(targetRange.dateTo) && (!this.includesFrom || !targetRange.includesTo)))
        {
            return (DateRange)DefaultEmptyRange.clone();
        }

        if(targetRange.dateFrom.isAfter(this.dateTo)
                || (targetRange.dateFrom.equals(this.dateTo) && (!targetRange.includesFrom || !this.includesTo)))
        {
            return (DateRange)DefaultEmptyRange.clone();
        }

        LocalDate intersectionFrom = DateUtils.max(this.dateFrom, targetRange.dateFrom);
        boolean intersectionIncludesFrom = this.includes(intersectionFrom) && targetRange.includes(intersectionFrom);

        LocalDate intersectionTo = DateUtils.min(this.dateTo, targetRange.dateTo);
        boolean intersectionIncludesTo = this.includes(intersectionTo) && targetRange.includes(intersectionTo);

        return new DateRange(intersectionFrom, intersectionTo, intersectionIncludesFrom, intersectionIncludesTo);
    }

    public boolean intersectsWith(@NotNull DateRange targetRange)
    {
        Objects.requireNonNull(targetRange,"targetRange is null");

        DateRange intersection = getIntersection(targetRange);
        return !intersection.isEmpty();
    }

    public List<DateRange> subtruct(DateRange range){
        Objects.requireNonNull(range,"range is null");

        ArrayList<DateRange> result = new ArrayList<>();

        if (range.isEmpty()) {
            result.add(range);
            return result;
        }

        DateRange intersection = this.getIntersection(range);
        if (intersection.isEmpty())
            result.add(this);
        else {
            if (this.getMinDate().isBefore(intersection.getMinDate())) {
                result.add(DateRange.ofRightOpenRange(this.getMinDate(), intersection.getMinDate()));
            }
            if (this.getMaxDate().isAfter(intersection.getMaxDate())) {
                result.add(DateRange.ofLeftOpenRange(intersection.getMaxDate(), this.getMaxDate()));
            }
        }
        return result;
    }

    public boolean includes(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date,"date is null");

        if(date.isAfter(dateFrom) && date.isBefore(dateTo))
            return true;

        return (date.equals(dateFrom) && includesFrom) || (date.equals(dateTo) && includesTo);
    }

    public boolean includes(@NotNull DateRange range)
    {
        Objects.requireNonNull(range,"range is null");

        DateRange intersection = getIntersection(range);
        return intersection.equals(range);
    }

    public Period getPeriodBetween()
    {
        if (isEmpty())
            return Period.ZERO;

        LocalDate periodStartInclusive = includesFrom ? dateFrom : dateFrom.plusDays(1);
        LocalDate periodEndExclusive = includesTo ? dateTo.plusDays(1) : dateTo;
        return Period.between(periodStartInclusive, periodEndExclusive);
    }

    public Duration getDurationBetween()
    {
        if (isEmpty())
            return Duration.ZERO;

        LocalDate periodStartInclusive = includesFrom ? dateFrom : dateFrom.plusDays(1);
        LocalDate periodEndExclusive = includesTo ? dateTo.plusDays(1) : dateTo;
        return Duration.ofDays(ChronoUnit.DAYS.between(periodStartInclusive, periodEndExclusive));
    }

    public BigDecimal getExactYearsBetween()
    {
        if (isEmpty())
            return BigDecimal.ZERO;

        LocalDate periodStartInclusive = includesFrom ? dateFrom : dateFrom.plusDays(1);
        LocalDate periodEndExclusive = includesTo ? dateTo.plusDays(1) : dateTo;

        return DateUtils.yearsExact(periodStartInclusive, periodEndExclusive);
    }

    public BigDecimal getExactMonthsBetween()
    {
        if (isEmpty())
            return BigDecimal.ZERO;

        LocalDate periodStartInclusive = includesFrom ? dateFrom : dateFrom.plusDays(1);
        LocalDate periodEndExclusive = includesTo ? dateTo.plusDays(1) : dateTo;

        return DateUtils.monthsExact(periodStartInclusive, periodEndExclusive);
    }

    /*
    * Количество разных месяцев, пересекающихся с периодом (попадающих в период).
    * Например, любой период в 1 день вернет значение "1",
    * а период "[31.05.2020-01.06.2020]" вернет значение "2"
    * */
    public Integer getMonthsBetween()
    {
        if (isEmpty()) {
            return 0;
        }

        LocalDate periodStartInclusive = getMinDate();
        LocalDate periodEndExclusive = getMaxDateExcluded();

        return (periodEndExclusive.getMonthValue() + periodEndExclusive.getYear() * 12)
            - (periodStartInclusive.getMonthValue() + periodStartInclusive.getYear() * 12);
    }

    public boolean startsAfter(@NotNull DateRange range)
    {
        Objects.requireNonNull(range, "range is null");

        return getMinDate().isAfter(range.getMaxDate());
    }

    public boolean startsAfter(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return getMinDate().isAfter(date);
    }

    public boolean startsAfterOrOn(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return !date.isAfter(getMinDate());
    }

    public boolean endsBefore(@NotNull DateRange range)
    {
        Objects.requireNonNull(range,"range is null");

        return getMaxDate().isBefore(range.getMinDate());
    }

    public boolean endsBefore(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return date.isAfter(getMaxDate());
    }

    public boolean endsBeforeOrOn(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return !date.isBefore(getMaxDate());
    }

    public boolean startsBefore(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return date.isAfter(getMinDate());
    }

    public boolean startsBeforeOrOn(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return !date.isBefore(getMinDate());
    }

    public boolean endsAfter(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return getMaxDate().isAfter(date);
    }

    public boolean endsAfterOrOn(@NotNull LocalDate date)
    {
        Objects.requireNonNull(date, "date is null");

        return !getMaxDate().isBefore(date);
    }

    @Override
    public String toString(){
        return String.format("%s%s; %s%s",
                includesFrom ? "[" : "(",
                DateUtils.formatRuDate(dateFrom),
                DateUtils.formatRuDate(dateTo),
                includesTo ? "]" : ")");
    }
}
