package ru.audithon.common.periodscalculator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.util.Assert;
import ru.audithon.common.types.DateRange;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PeriodCalculatorOld {
    private final List<DateRangeInfo> _list = new ArrayList<DateRangeInfo>();

    public PeriodCalculatorOld()
    {
        // Для обеспечения непрерывности добавим период без признаков, покрывающий весь временной диапазон - теперь
        //наша задача сведётся просто к дроблению этого периода при добавлении новых периодов со своими признаками
        _list.add(new DateRangeInfo(DateRange.ofClosedRange(LocalDate.MIN, LocalDate.MAX)));
    }

    public <T> PeriodCalculatorOld(DateRange dateRange, IdDataType<T> id, T data) {
        this();
        addPeriod(dateRange, id, data);
    }

    public <T> void addPeriod(DateRange dateRange, IdDataType<T> idDataType, T data) {
        addPeriod(dateRange, idDataType.getDataId(), data);
    }

    public void addPeriod(DateRange dateRange, int id, Object data)
    {
        addPeriod(dateRange, id, data, null);
    }

    /**
     * Разбивает набор периодов в калькуляторе, исходя из новых данных по указанному ключу в укказанном преиоде.
     * @param dateRange Период
     * @param id Ключ даннных в периоде
     * @param data Новые данные по ключу в периоде
     * @param affectedPeriodDataGenerator Функция-генератор данных для нового периода, принимающее старое значение,
     *                                    добавленное в период значение и возвращающая значение, сохраняемое в добавленном периоде.
     *                                    По умолчанию просто заменяет старые данные новыми.
     */
    public <T> void addPeriod(DateRange dateRange, int id, T data, BiFunction<T, T, T> affectedPeriodDataGenerator)
    {
        if(dateRange.isEmpty())
        {
            return;
        }

        for(DateRangeInfo info: prepareAffectedPeriods(dateRange))
        {
            info.updateId(id,
                affectedPeriodDataGenerator != null
                    ? affectedPeriodDataGenerator.apply(info.get(id), data)
                    : data);
        }
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

    public DateRangeInfo[] getPeriods(int id, Object value)
    {
        HashMap<Integer, Object> signs = new HashMap<Integer, Object>();
        signs.put(id, value);
        return getPeriods(signs);
    }

    public <T> DateRangeInfo[] getPeriods(IdDataType<T> id, T value) {
        return getPeriods(id.getDataId(), value);
    }

    public <T> DateRangeInfo[] getPeriods(Map<IdDataType<T>, T> signs) {
        HashMap<Integer, Object> signsWithInt = new HashMap<>();
        signs.forEach((key, value) -> signsWithInt.put(key.getDataId(), value));
        return getPeriods(signsWithInt);
    }

    public DateRangeInfo[] getPeriods(HashMap<Integer, Object> signs)
    {
        ArrayList<DateRangeInfo> retVal = new ArrayList<DateRangeInfo>();

        for(DateRangeInfo t: _list)
        {
            if (t.hasSigns(signs))
                retVal.add(t);
        }

        return retVal.toArray(new DateRangeInfo[0]);
    }

    public <T> List<DateRangeInfo> getClonedPeriods(IdDataType<T> id, T value) {
        return getClonedPeriods(id.getDataId(), value);
    }

    public List<DateRangeInfo> getClonedPeriods(int id, Object value)
    {
        HashMap<Integer, Object> signs = new HashMap<Integer, Object>();
        signs.put(id, value);

        return Collections.unmodifiableList(getClonedPeriods(signs));
    }

    private List<DateRangeInfo> getClonedPeriods(HashMap<Integer, Object> signs)
    {
        ArrayList<DateRangeInfo> retVal = new ArrayList<DateRangeInfo>();

        for (DateRangeInfo t : _list)
        {
            if (t.hasSigns(signs))
                retVal.add(t.clone());
        }

        return Collections.unmodifiableList(retVal);
    }


    public List<DateRangeInfo> getPeriods()
    {
        return Collections.unmodifiableList(_list);
    }

    //    public <T> DateRangeInfo[] getPeriods(Predicate<Map<IdDataType<T>, T>> periodDataTester) {
//        ArrayList<DateRangeInfo> retVal = new ArrayList<DateRangeInfo>();
//
//        for (DateRangeInfo t : _list)
//        {
//            if (t.getPeriodData().size() == 0)
//                continue;
//
//            if (periodDataTester.test(t.getPeriodData()))
//                retVal.add(t);
//        }
//
//        return retVal.toArray(new DateRangeInfo[0]);
//    }
//
    public DateRangeInfo[] getPeriods(Predicate<Map<Integer, Object>> periodDataTester)
    {
        ArrayList<DateRangeInfo> retVal = new ArrayList<DateRangeInfo>();

        for (DateRangeInfo t : _list)
        {
            if (t.getPeriodData().size() == 0)
                continue;

            if (periodDataTester.test(t.getPeriodData()))
                retVal.add(t);
        }

        return retVal.toArray(new DateRangeInfo[0]);
    }

    public List<DateRangeInfo> getPredicatedPeriods(Predicate<DateRangeInfo> periodDataTester)
    {
        Objects.requireNonNull(periodDataTester);

        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        for (DateRangeInfo t : _list)
        {
            if (t.getPeriodData().size() == 0)
                continue;

            if (periodDataTester.test(t))
                retVal.add(t);
        }

        return retVal;
    }

    public List<DateRangeInfo> getClonedPredicatedPeriods(Predicate<DateRangeInfo> periodDataTester)
    {
        Objects.requireNonNull(periodDataTester);

        ArrayList<DateRangeInfo> retVal = new ArrayList<>();

        for (DateRangeInfo t : _list)
        {
            if (t.getPeriodData().size() == 0)
                continue;

            if (periodDataTester.test(t))
                retVal.add(t.clone());
        }

        return retVal;
    }

    public List<DateRange> getPredicatedRanges(Predicate<DateRangeInfo> periodDataTester)
    {
        return getPredicatedPeriods(periodDataTester).stream().map(DateRangeInfo::getDateRange).collect(Collectors.toList());
    }

    public DateRangeInfo getPeriod(LocalDate date)
    {
        for (DateRangeInfo t : _list)
        {
            if (t.getDateRange().includes(date))
                return t;
        }

        return null;
    }

    /// <summary>
    ///     Объединяет подряд идущие одинаковые периоды в один период
    /// </summary>
    public void normalize()
    {
        int index = 1;

        while (index < _list.size())
        {
            if (_list.get(index - 1).hasEqualSigns(_list.get(index)))
            {
                _list.get(index - 1).setDateTo(_list.get(index).getDateRange().getDateTo());
                _list.remove(index);
            }
            else
            {
                index++;
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
                    if (period.getPayload() != null) { add(period.getPayload()); }
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
                    if (period.getPayload() != null) { add(period.getPayload()); }
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

    private Collection<DateRangeInfo> prepareAffectedPeriods(DateRange dateRange)
    {
        DateRange affectingRange = DateRange.ofClosedRange(dateRange);

        LocalDate dateFrom = affectingRange.getDateFrom();
        LocalDate dateTo = affectingRange.getDateTo();

        ArrayList<DateRangeInfo> result = new ArrayList<DateRangeInfo>();

        int start = getPeriodListIndex(dateFrom);

        // Обработаем имеющиеся периоды, которые полностью перекрываются переданным периодом -
        //в таких периодах необходимо установить переданный признак
        int end = start;
        while (end < _list.size())
        {
            DateRangeInfo currentItem = _list.get(end);
            DateRange currentItemDateRange = currentItem.getDateRange();

            // выходим из цикла обработки если добрались до периодов, которые позже переданного периода
            if (currentItemDateRange.startsAfter(affectingRange))
                break;

            // теперь обработаем период, который пересекается с переданным периодом (либо он такой один, либо его вообще нет) -
            //в зависимости от типа пересечения разобъём этот период на несколько
            if (affectingRange.includes(currentItemDateRange))
            {
                result.add(currentItem);
            }
            else if (currentItemDateRange.startsAfterOrOn(dateFrom)
                && currentItemDateRange.endsAfter(dateTo))
            {
                // Случай №1: переданный период пересекает имеющийся период слева - надо разбить
                //имеющийся период на 2 части и в левую часть добавить переданный признак, а правую часть
                //оставить без изменений

                DateRangeInfo newPi = currentItem.clone(); // добавляем новый с переданным признаком
                result.add(newPi);
                newPi.setDateFrom(currentItemDateRange.getDateFrom());
                newPi.setDateTo(dateTo);

                currentItem.setDateFrom(dateTo.plusDays(1)); // обрезаем имеющийся слева
                _list.add(end, newPi);
                end++;
            }
            else if (currentItemDateRange.startsBefore(dateFrom) && currentItemDateRange.endsBeforeOrOn(dateTo))
            {
                // Случай №2: переданный период пересекает имеющийся период справа - надо разбить
                //имеющийся период на 2 части и в правую часть добавить переданный признак, а левую часть
                //оставить без изменений

                DateRangeInfo newPi = currentItem.clone(); // добавляем новый с переданным признаком
                result.add(newPi);
                newPi.setDateFrom(dateFrom);
                newPi.setDateTo(currentItemDateRange.getDateTo());
                currentItem.setDateTo(dateFrom.minusDays(1)); // обрезаем имеющийся слева
                _list.add(end + 1, newPi);
                end++;
            }
            else if (currentItemDateRange.startsBefore(dateFrom) && currentItemDateRange.endsAfter(dateTo))
            {
                // Случай №3: переданный период находится целиком внутри имеющегося периода - надо разбить
                //имеющийся период на 3 части и в центральную часть добавить переданный признак, а левую и правую части
                //оставить без изменений
                DateRangeInfo newPi1 = currentItem.clone(); // добавляем левую часть
                newPi1.setDateFrom(currentItemDateRange.getDateFrom());
                newPi1.setDateTo(dateFrom.minusDays(1));
                _list.add(end, newPi1);

                DateRangeInfo nextItem = _list.get(end + 1);

                DateRangeInfo newPi2 = currentItem.clone(); // добавляем правую часть
                newPi2.setDateFrom(dateTo.plusDays(1));
                newPi2.setDateTo(nextItem.getMaxDate());
                _list.add(end + 2, newPi2);

                result.add(nextItem); // добавляем переданный признак в центральную часть
                nextItem.setDateFrom(dateFrom); // обрезаем имеющийся слева
                nextItem.setDateTo(dateTo); // обрезаем имеющийся справа
                end += 2;
            }

            end++;
        }

        return result;
    }

    /**
     Находит первый от начала период, который оканчивается позже, чем начинается
     указанный в параметрах период (т.е. все периоды до найденного будут ранее указанного и
     не будут с ним пересекаться, а найденный период будет либо пересекаться с переданным периодом,
     либо будет позже переданного периода)
     */
    private int getPeriodListIndex(LocalDate dateFrom)
    {
        int i = 0;
        for (DateRangeInfo item: _list)
        {
            //is after or equal
            if (!item.getDateRange().getDateTo().isBefore(dateFrom))
                return i;
            i++;
        }

        return _list.size();
    }
}
