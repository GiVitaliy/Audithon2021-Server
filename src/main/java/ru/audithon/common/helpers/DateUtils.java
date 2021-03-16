package ru.audithon.common.helpers;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Objects;

public final class DateUtils {
    public static final LocalDate MaxDate = LocalDate.of(9999, 12, 31);
    public static final LocalDate DefaultDate = LocalDate.of(1900, 1, 1);
    public static final LocalDate DefaultDate2 = LocalDate.of(1800, 1, 1);

    public static LocalDate decDateToExcluded(LocalDate date) {
        if (date == null) return null;
        if (MaxDate.equals(date)) return date;
        if (LocalDate.MIN.equals(date)) return date;

        return date.minusDays(1);
    }

    public static LocalDate incDateFromExcluded(LocalDate date) {
        return incDateWithMaxLimit(date);
    }

    public static LocalDate incDateWithMaxLimit(LocalDate date) {
        if (date == null) return null;
        if (MaxDate.equals(date)) return date;

        return date.plusDays(1);
    }

    public static LocalDate max(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    public static LocalDateTime max(LocalDateTime date1, LocalDateTime date2) {
        return date1.isAfter(date2) ? date1 : date2;
    }

    public static LocalDate max(LocalDate date1, LocalDate date2, LocalDate date3) {
        LocalDate tempMax = date1.isAfter(date2) ? date1 : date2;
        return max(tempMax, date3);
    }

    public static LocalDate min(LocalDate date1, LocalDate date2) {
        return date1.isAfter(date2) ? date2 : date1;
    }

    public static LocalDateTime min(LocalDateTime date1, LocalDateTime date2) {
        return date1.isAfter(date2) ? date2 : date1;
    }

    public static LocalDate startOfTheNextMonth(LocalDate date) {
        return date.minusDays(date.getDayOfMonth() - 1).plusMonths(1);
    }

    public static LocalDate startOfThePrevMonth(LocalDate date) {
        return startOfTheMonth(date).minusMonths(1);
    }

    public static LocalDate startOfTheQuarter(LocalDate date) {
        return startOfTheMonth(date).minusMonths(((date.getMonthValue() - 1) % 3));
    }

    public static LocalDate startOfTheMonth(LocalDate date) {
        return startOfTheNextMonth(date).minusMonths(1);
    }

    public static LocalDate startOfTheMonth(LocalDateTime date) {
        return startOfTheMonth(date.toLocalDate());
    }

    public static LocalDate startOfTheYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 1, 1);
    }

    public static LocalDate startOfTheNextYear(LocalDate date) {
        return LocalDate.of(date.getYear() + 1, 1, 1);
    }

    public static LocalDate endOfTheMonth(LocalDate date) {
        return startOfTheNextMonth(date).minusDays(1);
    }

    public static LocalDate endOfThePrevMonth(LocalDate date) {
        return startOfTheMonth(date).minusDays(1);
    }

    public static LocalDate endOfTheYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 12, 31);
    }

    public static BigDecimal monthsExact(LocalDate dateFromInclusive, LocalDate dateToExclusive) {
        dateToExclusive = dateToExclusive.minusDays(1);

        Period periodBetweenMonths = Period.between(startOfTheMonth(dateFromInclusive), endOfTheMonth(dateToExclusive));
        BigDecimal fullMonthsInPeriod =
            BigDecimal.valueOf((periodBetweenMonths.getYears() * 12) + periodBetweenMonths.getMonths());
        BigDecimal dayOfMonthFrom = BigDecimal.valueOf(dateFromInclusive.getDayOfMonth() - 1);
        BigDecimal daysInMonthFrom =
            BigDecimal.valueOf(YearMonth.of(dateFromInclusive.getYear(), dateFromInclusive.getMonth()).lengthOfMonth());
        BigDecimal dayOfMonthTo = BigDecimal.valueOf(dateToExclusive.getDayOfMonth());
        BigDecimal daysInMonthTo =
            BigDecimal.valueOf(YearMonth.of(dateToExclusive.getYear(), dateToExclusive.getMonth()).lengthOfMonth());

        return fullMonthsInPeriod  // количество полных месяцев
            .subtract(          // отнимем часть месяца, с его начала до даты начала периода
                dayOfMonthFrom.divide(daysInMonthFrom, 50, BigDecimal.ROUND_HALF_UP))
            .add(               // прибавим часть месяца, в которую попала дата окончания периода
                dayOfMonthTo.divide(daysInMonthTo, 50, BigDecimal.ROUND_HALF_UP));
    }

    public static BigDecimal yearsExact(LocalDate dateFromInclusive, LocalDate dateToExclusive) {
        return monthsExact(dateFromInclusive, dateToExclusive).divide(BigDecimal.valueOf(12), 50, BigDecimal.ROUND_HALF_UP);
    }

    public static Integer fullYearsBetween(LocalDate dateFromInclusive, LocalDate dateToInclusive) {
        return yearsExact(dateFromInclusive, dateToInclusive.plusDays(1)).intValue();
    }

    public static String formatISODate(LocalDate date) {
        return formatInternal(date, isoFormatterDate);
    }

    public static String formatISODateTime(LocalDateTime dateTime) {
        return formatInternal(dateTime, DateTimeFormatter.ISO_DATE_TIME);
    }

    public static String formatXml81Date(LocalDate date) {
        return formatInternal(date, xml81FormatterDate);
    }

    public static String formatCentrZanDate(LocalDate date) { return formatInternal(date, centrZanFormatterDate); }

    public static String formatPfrDate(LocalDate date) { return formatInternal(date, pfrFormatterDate); }

    public static String formatRuDate(LocalDateTime dateTime) {
        return formatInternal(dateTime, ruFormatterDate);
    }

    public static String formatRuDate(LocalDate date) { return formatInternal(date, ruFormatterDate); }

    public static String formatRuDateShort(LocalDateTime dateTime) {
        return formatInternal(dateTime, ruFormatterDateShort);
    }

    public static String formatRuDateShort(LocalDate date) {
        return formatInternal(date, ruFormatterDateShort);
    }

    public static String formatRuDateLiteral(LocalDate date) {
        return formatInternal(date, ruFormatterDateLiteral);
    }

    public static String formatRuMonth(LocalDate date) {
        return formatInternal(date, ruMonthFormatter);
    }

    public static String formatRuMonthShort(LocalDate date) {
        return formatRuMonthShort(date, false);
    }

    public static String formatRuMonthShort(LocalDate date, Boolean standaloneMonth) {
        if (standaloneMonth) {
            return String.format("%s %s",
                    Month.from(date).getDisplayName(TextStyle.SHORT_STANDALONE, ruMonthShortFormatter.getLocale()),
                    date.getYear());
        }

        return formatInternal(date, ruMonthShortFormatter);
    }

    public static String formatRuDateTime(LocalDateTime dateTime) {
        return dateTime.format(ruFormatterDateTime);
    }

    public static LocalDateTime parseRuDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, ruFormatterDateTime);
    }

    public static LocalDateTime parseXmlDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, xmlFormatterDateTime);
    }

    public static LocalDate parseXmlDate(String date) {
        return LocalDate.parse(date, xmlFormatterDate);
    }

    public static LocalDate parseRuDate(String date) {
        return LocalDate.parse(date, ruFormatterDate);
    }

    public static LocalDate parseISODate(String date) {
        return LocalDate.parse(date, isoFormatterDate);
    }

    public static LocalDate parseISODateNullable(String date) {
        if (date == null) {
            return null;
        }

        return LocalDate.parse(date, isoFormatterDate);
    }

    public static int toInt(LocalDate date) {
        Objects.requireNonNull(date);

        long longDate = date.toEpochDay();
        long maxIntDate = (long)Integer.MAX_VALUE;
        long minIntDate = (long)Integer.MIN_VALUE;

        if (minIntDate <= longDate && longDate <= maxIntDate) {
            return (int)longDate;
        } else {
            throw new IllegalArgumentException(String.format("Дата %s выходит за пределы конвертации в целое число", formatRuDate(date)));
        }
    }

    public static LocalDate ofInt(Integer epochDay) {
        Objects.requireNonNull(epochDay);

        return LocalDate.ofEpochDay(epochDay);
    }

    public static LocalDate ofXmlGregorianCalendar(XMLGregorianCalendar xgcDate) {
        if (xgcDate == null) {
            return null;
        }

        return LocalDate.of(xgcDate.getYear(), xgcDate.getMonth(), xgcDate.getDay());
    }

    public static LocalDateTime ofXmlGregorianCalendarTime(XMLGregorianCalendar xgcDate) {
        if (xgcDate == null) {
            return null;
        }

        if (xgcDate.getHour() < 0) {
            return LocalDateTime.of(xgcDate.getYear(), xgcDate.getMonth(), xgcDate.getDay(),
                0, 0, 0);
        }

        return LocalDateTime.of(xgcDate.getYear(), xgcDate.getMonth(), xgcDate.getDay(),
            xgcDate.getHour(), xgcDate.getMinute(), xgcDate.getSecond());
    }

    private static String formatInternal(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null)
            return "";

        return dateTime.format(formatter);
    }

    private static String formatInternal(LocalDate date, DateTimeFormatter formatter) {
        if (date == null)
            return "";

        return date.format(formatter);
    }

    public static final DateTimeFormatter ruFormatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter ruFormatterDateShort = DateTimeFormatter.ofPattern("dd.MM.yy");
    public static final DateTimeFormatter pfrFormatterDate = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter ruFormatterDateLiteral = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter ruFormatterDateTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter isoFormatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter xmlFormatterDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter xmlFormatterDateTime = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    private static final DateTimeFormatter ruMonthFormatter = DateTimeFormatter.ofPattern("LLLL, yyyy");
    private static final DateTimeFormatter ruMonthShortFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
    private static final DateTimeFormatter xml81FormatterDate = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter centrZanFormatterDate = DateTimeFormatter.ofPattern("yyyy/MM/dd");
}
