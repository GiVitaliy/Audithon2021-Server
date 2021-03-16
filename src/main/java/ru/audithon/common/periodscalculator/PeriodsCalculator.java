package ru.audithon.common.periodscalculator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.types.DatePeriod;
import ru.audithon.common.types.DateRange;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class PeriodsCalculator {

    private static class PeriodsBucket {
        private DateRangeInfo info;
        private PeriodsBucket next;

        public PeriodsBucket(DateRangeInfo info, PeriodsBucket next) {
            this.info = info;
            this.next = next;
        }
    }

    // Для обеспечения непрерывности добавим период без признаков, покрывающий весь временной диапазон - теперь
    //наша задача сведётся просто к дроблению этого периода при добавлении новых периодов со своими признаками
    private final PeriodsBucket _list = new PeriodsBucket(
        new DateRangeInfo(DateRange.ofClosedRange(LocalDate.MIN, LocalDate.MAX)), null);

    public PeriodsCalculator() {
    }

    public <T> PeriodsCalculator(DateRange dateRange, IdDataType<T> id, T data) {
        this();
        addPeriod(dateRange, id, data);
    }

    public <T> void addPeriod(DateRange dateRange, IdDataType<T> idDataType, T data) {
        addPeriod(dateRange, idDataType.getDataId(), data);
    }

    public void addPeriod(DateRange dateRange, int id, Object data) {
        addPeriod(dateRange, id, data, null);
    }

    /**
     * Разбивает набор периодов в калькуляторе, исходя из новых данных по указанному ключу в укказанном преиоде.
     *
     * @param dateRange                   Период
     * @param id                          Ключ даннных в периоде
     * @param data                        Новые данные по ключу в периоде
     * @param affectedPeriodDataGenerator Функция-генератор данных для нового периода, принимающее старое значение,
     *                                    добавленное в период значение и возвращающая значение, сохраняемое в добавленном периоде.
     *                                    По умолчанию просто заменяет старые данные новыми.
     */
    public <T> void addPeriod(DateRange dateRange, int id, T data, BiFunction<T, T, T> affectedPeriodDataGenerator) {
        if (dateRange.isEmpty()) {
            return;
        }

        prepareAffectedPeriods(dateRange, info -> info.updateId(id,
            affectedPeriodDataGenerator != null
                ? affectedPeriodDataGenerator.apply(info.get(id), data)
                : data));
    }

    public <T> void addPeriod(DateRange dateRange, IdDataType<T> id, T data, BiFunction<T, T, T> affectedPeriodDataGenerator) {
        addPeriod(dateRange, id.getDataId(), data, affectedPeriodDataGenerator);
    }

    public <T> void addPeriods(IdDataType<T> id, Collection<PayloadDateRange<T>> periodicData) {
        periodicData.forEach(payloadRange -> {
            addPeriod(payloadRange.getDateRange(), id, payloadRange.getPayload());
        });
    }

    public <TItem, TItemPayload> void addPeriods(IdDataType<TItemPayload> id, Collection<TItem> periodicData,
                                                 Function<TItem, DateRange> itemRangeSupplier,
                                                 Function<TItem, TItemPayload> itemPayloadSupplier) {
        periodicData.forEach(item -> {
            addPeriod(itemRangeSupplier.apply(item), id, itemPayloadSupplier.apply(item));
        });
    }

    public List<DateRangeInfo> getPeriods(int id, Object value) {
        HashMap<Integer, Object> signs = new HashMap<>();
        signs.put(id, value);
        return getPeriods(signs);
    }

    public <T> List<DateRangeInfo> getPeriods(IdDataType<T> id, T value) {
        return getPeriods(id.getDataId(), value);
    }

    public <T> List<DateRangeInfo> getPeriods(Map<IdDataType<T>, T> signs) {
        HashMap<Integer, Object> signsWithInt = new HashMap<>();
        signs.forEach((key, value) -> signsWithInt.put(key.getDataId(), value));
        return getPeriods(signsWithInt);
    }

    public List<DateRangeInfo> getPeriods(HashMap<Integer, Object> signs) {
        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.hasSigns(signs))
                retVal.add(node.info);

            node = node.next;
        }

        return retVal;
    }

    public <T> List<DateRangeInfo> getClonedPeriods(IdDataType<T> id, T value) {
        return getClonedPeriods(id.getDataId(), value);
    }

    public List<DateRangeInfo> getClonedPeriods(int id, Object value) {
        HashMap<Integer, Object> signs = new HashMap<>();
        signs.put(id, value);

        return getClonedPeriods(signs);
    }

    private List<DateRangeInfo> getClonedPeriods(HashMap<Integer, Object> signs) {
        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.hasSigns(signs))
                retVal.add(node.info.clone());

            node = node.next;
        }

        return Collections.unmodifiableList(retVal);
    }


    public List<DateRangeInfo> getPeriods() {
        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            retVal.add(node.info);

            node = node.next;
        }

        return Collections.unmodifiableList(retVal);
    }

    public List<DateRangeInfo> getPeriods(Predicate<Map<Integer, Object>> periodDataTester) {
        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.getPeriodData().size() > 0 && periodDataTester.test(node.info.getPeriodData()))
                retVal.add(node.info);

            node = node.next;
        }

        return retVal;
    }

    public List<DateRangeInfo> getPredicatedPeriods(Predicate<DateRangeInfo> periodDataTester) {
        Objects.requireNonNull(periodDataTester);

        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.getPeriodData().size() > 0 && periodDataTester.test(node.info))
                retVal.add(node.info);

            node = node.next;
        }

        return retVal;
    }

    public List<DateRangeInfo> getClonedPredicatedPeriods(Predicate<DateRangeInfo> periodDataTester) {
        Objects.requireNonNull(periodDataTester);

        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.getPeriodData().size() > 0 && periodDataTester.test(node.info))
                retVal.add(node.info.clone());

            node = node.next;
        }

        return retVal;
    }

    public List<DateRange> getPredicatedRanges(Predicate<DateRangeInfo> periodDataTester) {
        return getPredicatedPeriods(periodDataTester).stream().map(DateRangeInfo::getDateRange).collect(Collectors.toList());
    }

    public DateRangeInfo getPeriod(LocalDate date) {
        PeriodsBucket node = _list;
        while (node != null) {
            if (node.info.getDateRange().includes(date))
                return node.info;

            node = node.next;
        }

        return null;
    }

    public void normalize() {
        PeriodsBucket current = _list;

        while (current.next != null) {
            if (current.info.hasEqualSigns(current.next.info)) {
                current.info.setDateTo(current.next.info.getDateRange().getDateTo());
                current.next = current.next.next;
            } else {
                current = current.next;
            }
        }
    }

    public static List<DateRange> combinePeriods(Collection<DateRange> periods) {
        Assert.notNull(periods, "periods can't be null");

        if (periods.isEmpty()) return Lists.newArrayList();

        PeriodsCalculator calc = new PeriodsCalculator();
        periods.forEach(period -> {
            if (period.isEmpty()) return;

            calc.addPeriod(period, 100500, true);
        });
        calc.normalize();
        return calc.getPredicatedRanges(periodInfo -> Objects.equals(periodInfo.get(100500), true));
    }

    public static <T> List<PayloadDateRange<T>> mergeIntersectedPeriods(List<PayloadDateRange<T>> periods,
                                                                        Function<Collection<T>, T> intersectedPayloadCombiner) {
        Assert.notNull(periods, "periods can't be null");
        Assert.notNull(intersectedPayloadCombiner, "intersectedPayloadCombiner can't be null");

        if (periods.isEmpty()) return Lists.newArrayList();

        final IdDataType<Set<T>> dataSign = new IdDataType<>(1);

        PeriodsCalculator calc = new PeriodsCalculator();
        periods.forEach(period -> {
            calc.addPeriod(period.getDateRange(), dataSign,
                new HashSet<T>() {{
                    if (period.getPayload() != null) {
                        add(period.getPayload());
                    }
                }},
                (Set<T> oldSet, Set<T> newSet) -> {
                    if (oldSet == null) {
                        return newSet;
                    }
                    if (newSet == null) {
                        return oldSet;
                    }
                    if (oldSet.equals(newSet)) {
                        return oldSet;
                    }
                    return Sets.union(oldSet, newSet);
                });
        });

        calc.normalize();

        return calc.getPredicatedPeriods(p -> p.get(dataSign) != null).stream()
            .map(periodInfo -> new PayloadDateRange<>(periodInfo.getDateRange(),
                intersectedPayloadCombiner.apply(
                    periodInfo.get(dataSign).stream()
                        .distinct().collect(Collectors.toList()))
            )).collect(Collectors.toList());
    }

    public static <T> List<PayloadDateRange<Collection<T>>> mergeIntersectedPeriodsWithCollection(List<PayloadDateRange<T>> periods,
                                                                                                  Function<Collection<T>, Collection<T>> intersectedPayloadCombiner) {
        Assert.notNull(periods, "periods can't be null");
        Assert.notNull(intersectedPayloadCombiner, "intersectedPayloadCombiner can't be null");

        if (periods.isEmpty()) return Lists.newArrayList();

        final IdDataType<Set<T>> dataSign = new IdDataType<>(1);

        PeriodsCalculator calc = new PeriodsCalculator();
        periods.forEach(period -> {
            calc.addPeriod(period.getDateRange(), dataSign,
                new HashSet<T>() {{
                    if (period.getPayload() != null) {
                        add(period.getPayload());
                    }
                }},
                (Set<T> oldSet, Set<T> newSet) -> {
                    if (oldSet == null) {
                        return newSet;
                    }
                    if (newSet == null) {
                        return oldSet;
                    }
                    if (oldSet.equals(newSet)) {
                        return oldSet;
                    }
                    return Sets.union(oldSet, newSet);
                });
        });

        calc.normalize();

        return calc.getPredicatedPeriods(p -> p.get(dataSign) != null).stream()
            .map(periodInfo -> new PayloadDateRange<>(periodInfo.getDateRange(),
                intersectedPayloadCombiner.apply(
                    periodInfo.get(dataSign).stream()
                        .distinct().collect(Collectors.toList()))
            )).collect(Collectors.toList());
    }


    public static <T> Collection<PayloadDateRange<Collection<T>>> invertPeriodicalData(Map<T, Collection<DateRange>> periodicalData) {
        Assert.notNull(periodicalData, "Объект с периодическими данными не может быть пустым");

        PeriodsCalculator periodsCalculator = new PeriodsCalculator();
        final int dataSign = 1;

        periodicalData.forEach(
            (data, rangeCollection) -> {
                List<DateRange> combinedRanges = combinePeriods(rangeCollection);
                combinedRanges.forEach(range -> periodsCalculator.addPeriod(range, dataSign,
                    new HashSet<T>() {{
                        add(data);
                    }},
                    (Set<T> oldSet, Set<T> newSet) -> {
                        if (oldSet == null) {
                            return newSet;
                        }
                        if (newSet == null) {
                            return oldSet;
                        }
                        if (oldSet.equals(newSet)) {
                            return oldSet;
                        }
                        return Sets.union(oldSet, newSet);
                    }));
            }
        );

        periodsCalculator.normalize();

        return periodsCalculator.getPredicatedPeriods(p -> p.get(dataSign) != null).stream()
            .map(periodInfo -> new PayloadDateRange<Collection<T>>(periodInfo.getDateRange(), periodInfo.get(dataSign)))
            .collect(Collectors.toList());
    }

    private void prepareAffectedPeriods(DateRange dateRange, Consumer<DateRangeInfo> consumer) {
        DateRange affectingRange = DateRange.ofClosedRange(dateRange);

        LocalDate dateFrom = affectingRange.getDateFrom();
        LocalDate dateTo = affectingRange.getDateTo();

        PeriodsBucket current = getPeriodListIndex(dateFrom);

        // Обработаем имеющиеся периоды, которые полностью перекрываются переданным периодом -
        //в таких периодах необходимо установить переданный признак
        while (current != null) {
            DateRangeInfo currentItem = current.info;
            DateRange currentItemDateRange = currentItem.getDateRange();

            // выходим из цикла обработки если добрались до периодов, которые позже переданного периода
            if (currentItemDateRange.startsAfter(affectingRange))
                break;

            // теперь обработаем период, который пересекается с переданным периодом (либо он такой один, либо его вообще нет) -
            //в зависимости от типа пересечения разобъём этот период на несколько
            if (affectingRange.includes(currentItemDateRange)) {
                consumer.accept(currentItem);
            } else if (currentItemDateRange.startsAfterOrOn(dateFrom)
                && currentItemDateRange.endsAfter(dateTo)) {
                // Случай №1: переданный период пересекает имеющийся период слева - надо разбить
                //имеющийся период на 2 части - левую часть оставить без изменений, а правую сделать новую и вставить
                // сразу за левой частью

                DateRangeInfo newPi = currentItem.clone(); // добавляем новый с переданным признаком
                newPi.setDateFrom(dateTo.plusDays(1)); // новый пойдет сразу после окончания имеющегося (обрезанного)

                current.info.setDateTo(dateTo); // а старый период обрезаем существующими
                current.next = new PeriodsBucket(newPi, current.next);

                consumer.accept(currentItem);

                current = current.next;
            } else if (currentItemDateRange.startsBefore(dateFrom) && currentItemDateRange.endsBeforeOrOn(dateTo)) {
                // Случай №2: переданный период пересекает имеющийся период справа - надо разбить
                //имеющийся период на 2 части и в правую часть добавить переданный признак, а левую часть
                //оставить без изменений

                DateRangeInfo newPi = currentItem.clone(); // добавляем новый с переданным признаком
                newPi.setDateFrom(dateFrom); // новый пойдет сразу после окончания имеющегося (обрезанного)

                current.info.setDateTo(dateFrom.minusDays(1)); // а старый период обрезаем существующими
                current.next = new PeriodsBucket(newPi, current.next);

                consumer.accept(newPi);

                current = current.next;
            } else if (currentItemDateRange.startsBefore(dateFrom) && currentItemDateRange.endsAfter(dateTo)) {
                // Случай №3: переданный период находится целиком внутри имеющегося периода - надо разбить
                //имеющийся период на 3 части и в центральную часть добавить переданный признак, а левую и правую части
                //оставить без изменений

                DateRangeInfo newPi1 = currentItem.clone(); // добавляем новый с переданным признаком
                newPi1.setDateFrom(dateFrom); // новый пойдет сразу после окончания имеющегося (обрезанного)
                newPi1.setDateTo(dateTo); // новый пойдет сразу после окончания имеющегося (обрезанного)

                DateRangeInfo newPi2 = currentItem.clone(); // добавляем новый с переданным признаком
                newPi2.setDateFrom(dateTo.plusDays(1)); // новый пойдет сразу после окончания имеющегося (обрезанного)

                consumer.accept(newPi1);

                current.info.setDateTo(dateFrom.minusDays(1)); // а старый период обрезаем существующими
                current.next = new PeriodsBucket(newPi1, current.next);

                current = current.next;

                current.next = new PeriodsBucket(newPi2, current.next);
                current = current.next;
            }

            current = current.next;
        }
    }

    /**
     * Находит первый от начала период, который оканчивается позже, чем начинается
     * указанный в параметрах период (т.е. все периоды до найденного будут ранее указанного и
     * не будут с ним пересекаться, а найденный период будет либо пересекаться с переданным периодом,
     * либо будет позже переданного периода)
     */
    private PeriodsBucket getPeriodListIndex(LocalDate dateFrom) {
        PeriodsBucket node = _list;

        while (node != null) {
            if (!node.info.getDateRange().getDateTo().isBefore(dateFrom))
                return node;

            node = node.next;
        }

        return null;
    }
}
