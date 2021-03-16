package ru.audithon.common.helpers;

import ru.audithon.common.types.DatePeriod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatePeriodUtils {


    public static <T> Collection<DatePeriod<T>> createDatePeriodFullCoverage(List<T> periodicObjects,
                                                                             LocalDateTime dateFromInclusive,
                                                                             LocalDateTime dateToExclusive,
                                                                             T defaultObject,
                                                                             Function<T, LocalDateTime> getDateFunction) {
        return createDatePeriodFullCoverageWithMapping(periodicObjects, dateFromInclusive, dateToExclusive,
            defaultObject, getDateFunction, x -> x);
    }

    // Создает сортированную по дате коллекцию на основе представленной коллекции periodicObjects таким образом, что
    //выходные периоды полностью покрывают период [dateFromInclusive, dateToExclusive).
    // periodicObjects содержит объекты, привязанные к какому-то временному периоду, имеющему начало действия и доступному
    // посредством getDateFunction
    public static <T, T2> Collection<DatePeriod<T2>> createDatePeriodFullCoverageWithMapping(List<T> periodicObjects,
                                                                                             LocalDateTime dateFromInclusive,
                                                                                             LocalDateTime dateToExclusive,
                                                                                             T2 defaultObject,
                                                                                             Function<T, LocalDateTime> getDateFunction,
                                                                                             Function<T, T2> resultMapping) {
        List<DatePeriod<T2>> rates = new ArrayList<>(periodicObjects.size());
        rates.add(new DatePeriod<>(dateFromInclusive, dateToExclusive, defaultObject));

        LocalDateTime lastDate = LocalDateTime.MIN;

        for (int i = 0; i < periodicObjects.size(); i++) {

            T rate = periodicObjects.get(i);

            // предполагаем что periodicObjects при первичном заполнении отсортированы по дате, но все равно проверяем, чтобы
            //исключить баги
            if (!lastDate.isBefore(getDateFunction.apply(rate))) {
                throw new IllegalArgumentException("Входящий массив periodicObjects не отсортирован. Работа невозможна.");
            } else {
                lastDate = getDateFunction.apply(rate);
            }

            if (!getDateFunction.apply(rate).isAfter(dateFromInclusive)) {
                rates.set(0, new DatePeriod<>(
                    dateFromInclusive,
                    i < periodicObjects.size() - 1 ? getDateFunction.apply(periodicObjects.get(i + 1)) : dateToExclusive,
                    resultMapping.apply(rate)));
            } else if (getDateFunction.apply(rate).isAfter(dateFromInclusive) && getDateFunction.apply(rate).isBefore(dateToExclusive)) {
                rates.get(rates.size() - 1).setDateTo(getDateFunction.apply(rate));
                rates.add(new DatePeriod<>(
                    getDateFunction.apply(rate),
                    i < periodicObjects.size() - 1 ? getDateFunction.apply(periodicObjects.get(i + 1)) : dateToExclusive,
                    resultMapping.apply(rate)));
            } else {
                break;
            }
        }

        rates.get(rates.size() - 1).setDateTo(dateToExclusive);

        return rates;
    }

    // Создает сортированную по дате коллекцию на основе представленной коллекции periodicObjects таким образом, что
    //выходные периоды полностью покрывают период [dateFromInclusive, dateToExclusive).
    // periodicObjects содержит объекты, привязанные к какому-то временному периоду, имеющему начало и конец действия
    //каждый следующий период может "затереть" предыдущий, если перекрывает его
    public static <T> Collection<DatePeriod<T>> createDatePeriodFullCoverage(List<T> periodicObjects,
                                                                             LocalDateTime dateFromInclusive,
                                                                             LocalDateTime dateToExclusive,
                                                                             T defaultObject,
                                                                             Function<T, LocalDateTime> getDateFromFunction,
                                                                             Function<T, LocalDateTime> getDateToFunction) {
        List<DatePeriod<T>> rates = new ArrayList<>(periodicObjects.size());
        rates.add(new DatePeriod<>(dateFromInclusive, dateToExclusive, defaultObject));

        // Предполагаем, что periodicObjects отсортированы в порядке возрастания их актуальности, т.е. каждый следующий
        //перетирает предыдущий, если они пересекаются
        for (int i = 0; i < periodicObjects.size(); i++) {

            T rate = periodicObjects.get(i);

            LocalDateTime oneDateFrom = getDateFromFunction.apply(rate);
            LocalDateTime oneDateTo = getDateToFunction.apply(rate);

            pushOnePeriod(rate, oneDateFrom, oneDateTo, rates, (_new, _existing) -> _new);
        }

        optimizePeriods(rates);

        return rates;
    }

    // Запихивает в коллекцию периодических объектов 'rates' переданный объект с переданным временным диапазоном.
    //Осуществляет необходимые разбиения частично пересекаемых периодов в 'rates'
    public static <T1, T2> void pushOnePeriod(T1 rate,
                                               LocalDateTime oneDateFrom,
                                               LocalDateTime oneDateTo,
                                               List<DatePeriod<T2>> rates,
                                               BiFunction<T1, T2, T2> mapFunction) {

        int oneDateFromIndex = findPeriodIndex(rates, oneDateFrom, 0);
        int oneDateToIndex = findPeriodIndex(rates, oneDateTo, oneDateFromIndex);

        // у нас rates полностью "без дыр" покарывают переданный период, поэтому пересечение можно представить исключительно
        // следующей комбинацией периодов:
        // 1) полное нахождение в одном из периодов
        // ИЛИ
        // 2) частичное или пустое пересечение периода слева
        // 3) полное или отсутствующее пересечение в середине
        // 4) частичное или пустое пересечение периода справа

        // И так: 1) полное нахождение в одном из периодов
        if (oneDateFromIndex < rates.size() && rates.get(oneDateFromIndex).includes(oneDateFrom)
            && rates.get(oneDateFromIndex).includes(oneDateTo)) {

            // разбиваем на 3 части

            // вторая часть - пересечение
            rates.add(oneDateFromIndex + 1, new DatePeriod<>(oneDateFrom, oneDateTo,
                mapFunction.apply(rate, rates.get(oneDateFromIndex).getPayload())));
            // третья часть - неизменная часть существующего периода справа
            rates.add(oneDateFromIndex + 2, new DatePeriod<>(oneDateTo, rates.get(oneDateFromIndex).getDateTo(), rates.get(oneDateFromIndex).getPayload()));

            // первой из трех частей (неизменная часть существующего периода слева) может и не быть,
            // если начало периодов совпадает
            if (rates.get(oneDateFromIndex).getDateFrom().equals(oneDateFrom)) {
                rates.remove(oneDateFromIndex);
            } else {
                rates.get(oneDateFromIndex).setDateTo(oneDateFrom);
            }
        } else {

            // 3) полное или отсутствующее пересечение в середине
            int fullCoverIndexFrom = oneDateFromIndex < rates.size() && !oneDateFrom.isAfter(rates.get(oneDateFromIndex).getDateFrom())
                ? oneDateFromIndex : oneDateFromIndex + 1;
            int fullCoverIndexTo = oneDateToIndex < rates.size() && !oneDateTo.isBefore(rates.get(oneDateToIndex).getDateTo())
                ? oneDateToIndex : oneDateToIndex - 1;
            for (int k = fullCoverIndexFrom; k <= fullCoverIndexTo; k++) {
                rates.get(k).setPayload(mapFunction.apply(rate, rates.get(k).getPayload()));
            }

            // 2) частичное или пустое пересечение периода слева
            if (oneDateFromIndex < rates.size() && oneDateFrom.isAfter(rates.get(oneDateFromIndex).getDateFrom())
                && rates.get(oneDateFromIndex).includes(oneDateFrom) && oneDateFromIndex < oneDateToIndex) {

                rates.add(oneDateFromIndex + 1, new DatePeriod<>(oneDateFrom,
                    rates.get(oneDateFromIndex).getDateTo(),
                    mapFunction.apply(rate, rates.get(oneDateFromIndex).getPayload())));
                rates.get(oneDateFromIndex).setDateTo(oneDateFrom);
                oneDateToIndex++;
            }

            // 4) частичное или пустое пересечение периода справа
            if (oneDateToIndex < rates.size() && oneDateTo.isBefore(rates.get(oneDateToIndex).getDateTo())
                && rates.get(oneDateToIndex).getDateFrom().isBefore(oneDateTo)) {

                rates.add(oneDateToIndex, new DatePeriod<>(rates.get(oneDateToIndex).getDateFrom(), oneDateTo,
                    mapFunction.apply(rate, rates.get(oneDateToIndex).getPayload())));
                rates.get(oneDateToIndex + 1).setDateFrom(oneDateTo);
            }

        }
    }

    // Пересекает 2 переданных периода. применяет к payload каждого из пересекшихся периодов функцию схлапывания mapFunction
    public static <T1, T2, T3> Collection<DatePeriod<T3>> intersectPeriods(Collection<DatePeriod<T1>> periodicObjects1,
                                                                           Collection<DatePeriod<T2>> periodicObjects2,
                                                                           BiFunction<T1, T2, T3> mapFunction) {

        DatePeriod<T1> totalPeriod1 = DatePeriod.ofCoverage(periodicObjects1);
        DatePeriod<T2> totalPeriod2 = DatePeriod.ofCoverage(periodicObjects2);
        DatePeriod<T1> totalPeriod = DatePeriod.ofIntersection(totalPeriod1, totalPeriod2.getDateFrom(), totalPeriod2.getDateTo());

        // Непитизированный выходной объект нам нужен, чтобы хранить промежуточные значения payload типа T1, до момента их
        //маппинга в T3
        List<DatePeriod<Object>> rates = new ArrayList<>((periodicObjects1.size() + periodicObjects2.size()) * 3 / 2);

        if (!totalPeriod.isEmptyOrNegative()) {
            rates.add(new DatePeriod<Object>(totalPeriod.getDateFrom(), totalPeriod.getDateTo(), null));
        }

        for (DatePeriod<T1> period1 : periodicObjects1) {
            pushOnePeriod(period1.getPayload(), period1.getDateFrom(), period1.getDateTo(),
                rates, (_new, _existing) -> _new);
        }

        for (DatePeriod<T2> period2 : periodicObjects2) {
            pushOnePeriod(period2.getPayload(), period2.getDateFrom(), period2.getDateTo(),
                rates, (_new, _existing) -> mapFunction.apply((T1) _existing, _new));
        }

        return rates.stream()
            .map(x -> new DatePeriod<T3>(x.getDateFrom(), x.getDateTo(), (T3) x.getPayload()))
            .collect(Collectors.toList());
    }

    // Объединяет 2 переданных периода. применяет к payload каждого из пересекшихся периодов функцию схлапывания mapFunction
    public static <T1, T2, T3> Collection<DatePeriod<T3>> unionPeriods(Collection<DatePeriod<T1>> periodicObjects1,
                                                                       Collection<DatePeriod<T2>> periodicObjects2,
                                                                       BiFunction<T1, T2, T3> mapFunction) {

        DatePeriod<T1> totalPeriod1 = DatePeriod.ofCoverage(periodicObjects1);
        DatePeriod<T2> totalPeriod2 = DatePeriod.ofCoverage(periodicObjects2);
        DatePeriod<Object> totalPeriod = new DatePeriod<>(
            minDateTime(totalPeriod1.getDateFrom(), totalPeriod2.getDateFrom()),
            maxDateTime(totalPeriod1.getDateTo(), totalPeriod2.getDateTo()), null);

        // Непитизированный выходной объект нам нужен, чтобы хранить промежуточные значения payload типа T1, до момента их
        //маппинга в T3
        List<DatePeriod<Object>> rates = new ArrayList<>((periodicObjects1.size() + periodicObjects2.size()) * 3 / 2);

        if (!totalPeriod.isEmptyOrNegative()) {
            rates.add(new DatePeriod<Object>(totalPeriod.getDateFrom(), totalPeriod.getDateTo(), null));
        }

        for (DatePeriod<T1> period1 : periodicObjects1) {
            pushOnePeriod(period1.getPayload(), period1.getDateFrom(), period1.getDateTo(),
                rates, (_new, _existing) -> _new);
        }

        for (DatePeriod<T2> period2 : periodicObjects2) {
            pushOnePeriod(period2.getPayload(), period2.getDateFrom(), period2.getDateTo(),
                rates, (_new, _existing) -> mapFunction.apply((T1) _existing, _new));
        }

        return rates.stream()
            .map(x -> new DatePeriod<T3>(x.getDateFrom(), x.getDateTo(), (T3) x.getPayload()))
            .collect(Collectors.toList());
    }

    // оптимизирует периоды - схлапывает подряд идущие
    private static <T> void optimizePeriods(List<DatePeriod<T>> rates) {

        int i = 0;

        while (i < rates.size() - 1) {
            if (rates.get(i).getPayload() == rates.get(i + 1).getPayload()) {
                rates.get(i).setDateTo(rates.get(i + 1).getDateTo());
                rates.remove(i + 1);
            } else {
                i++;
            }
        }
    }

    // Выполняет указанную операцию над "периодическими" объектами, попавшими в указанный период, указывая количество
    // дней пересечения с периодом (если неполное пересечение). Никак не изменяет состав периодов, не осуществляет их
    // "разбиение" и других подобных операций
    public static <T> void processPeriodicObjects(List<DatePeriod<T>> periodicObjects,
                                                  LocalDateTime dateFromInclusive,
                                                  LocalDateTime dateToExclusive,
                                                  BiConsumer<DatePeriod<T>, Integer> consumeLambda) {
        // если совсем не пересекаемся с периодом. то нечего и делать
        if (periodicObjects.size() == 0
            || !periodicObjects.get(0).getDateFrom().isBefore(dateToExclusive)
            || !periodicObjects.get(periodicObjects.size() - 1).getDateTo().isAfter(dateFromInclusive)) {
            return;
        }

        int i_from = findPeriodIndex(periodicObjects, dateFromInclusive, 0);
        int i_to = Math.min(findPeriodIndex(periodicObjects, dateToExclusive, i_from), periodicObjects.size() - 1);

        for (int i = i_from; i <= i_to; i++) {

            int days = (int) DatePeriod.ofIntersection(
                periodicObjects.get(i),
                dateFromInclusive,
                dateToExclusive).getTotalDays();

            if (days > 0) {
                consumeLambda.accept(periodicObjects.get(i), days);
            }
        }
    }

    // ищем период, в который включена указанная дата, начиная с переданного индекса i_start.
    // Если дата включена между периодами, возвращает индекс периода, перед которым находится искомая дата.
    //соответственно, если дата меньше всех периодов, возвращает "i_start", если дата больше всех периодов возвращает periodicObjects.size()
    public static <T> int findPeriodIndex(List<DatePeriod<T>> periodicObjects, LocalDateTime probeDate, int i_start) {

        if (periodicObjects.size() <= i_start || periodicObjects.get(i_start).getDateFrom().isAfter(probeDate)) {
            return i_start;
        } else if (!periodicObjects.get(periodicObjects.size() - 1).getDateTo().isAfter(probeDate)) {
            return periodicObjects.size();
        }

        // у нас все отсортировано, поэтому ищем делением пополам
        int i = i_start;
        int j = periodicObjects.size() - 1;
        while (i < j) {
            int k = (i + j) / 2;
            if (periodicObjects.get(k).includes(probeDate)) {
                return k;
            } else if (!periodicObjects.get(k).getDateTo().isAfter(probeDate)) {
                i = i < k ? k : i + 1;
            } else {
                j = k;
            }
        }

        return i;
    }

    public static <T> DatePeriod<T> findPeriod(List<DatePeriod<T>> periodicObjects, LocalDateTime probeDate, int i_start) {
        int i = findPeriodIndex(periodicObjects, probeDate, i_start);
        if (i < periodicObjects.size() && periodicObjects.get(i).includes(probeDate)) {
            return periodicObjects.get(i);
        } else {
            return null;
        }
    }

    // Выполняет пересечение "периодических" объектов с указанным периодом
    public static <T> List<DatePeriod<T>> intersectPeriodicObjects(List<DatePeriod<T>> periodicObjects,
                                                                   LocalDateTime dateFromInclusive,
                                                                   LocalDateTime dateToExclusive) {

        List<DatePeriod<T>> retVal = new ArrayList<>(periodicObjects.size());

        // если совсем не пересекаемся с периодом. то нечего и делать - возвращаем пустое пересечение
        if (periodicObjects.size() == 0
            || !periodicObjects.get(0).getDateFrom().isBefore(dateToExclusive)
            || !periodicObjects.get(periodicObjects.size() - 1).getDateTo().isAfter(dateFromInclusive)) {
            return retVal;
        }

        int i_from = findPeriodIndex(periodicObjects, dateFromInclusive, 0);
        int i_to = Math.min(findPeriodIndex(periodicObjects, dateToExclusive, i_from), periodicObjects.size() - 1);

        for (int i = i_from; i <= i_to; i++) {

            DatePeriod<T> period = DatePeriod.ofIntersection(
                periodicObjects.get(i),
                dateFromInclusive,
                dateToExclusive);

            if (!period.isEmptyOrNegative()) {
                retVal.add(period);
            }
        }

        return retVal;
    }

    // Проверяет пересечение "периодических" объектов с указанным периодом
    public static <T> boolean isPeriodicObjectsIntersects(List<DatePeriod<T>> periodicObjects,
                                                          DatePeriod<T> probePeriod) {
        return isPeriodicObjectsIntersects(periodicObjects, probePeriod.getDateFrom(), probePeriod.getDateTo());
    }

    public static <T> boolean isPeriodicObjectsIntersectDay(List<DatePeriod<T>> periodicObjects, LocalDate day) {
        return isPeriodicObjectsIntersects(periodicObjects,
            LocalDateTime.of(day, LocalTime.MIN), LocalDateTime.of(day, LocalTime.MIN).plusDays(1));
    }

    // Проверяет пересечение "периодических" объектов с указанным периодом
    public static <T> boolean isPeriodicObjectsIntersects(List<DatePeriod<T>> periodicObjects,
                                                          LocalDateTime dateFromInclusive,
                                                          LocalDateTime dateToExclusive) {

        // если совсем не пересекаемся с периодом. то нечего и делать - возвращаем пустое пересечение
        if (periodicObjects.size() == 0
            || !periodicObjects.get(0).getDateFrom().isBefore(dateToExclusive)
            || !periodicObjects.get(periodicObjects.size() - 1).getDateTo().isAfter(dateFromInclusive)
            || !dateToExclusive.isAfter(dateFromInclusive)) {
            return false;
        }

        int i_from = findPeriodIndex(periodicObjects, dateFromInclusive, 0);
        int i_to = Math.min(findPeriodIndex(periodicObjects, dateToExclusive, i_from), periodicObjects.size() - 1);

        for (int i = i_from; i <= i_to; i++) {

            DatePeriod<T> period = DatePeriod.ofIntersection(
                periodicObjects.get(i),
                dateFromInclusive,
                dateToExclusive);

            if (!period.isEmptyOrNegative()) {
                return true;
            }
        }

        return false;
    }

    //строит дополнение к представленному множеству периодов
    public static <T> List<DatePeriod<T>> inversePeriods(List<DatePeriod<T>> periodicObjects) {

        List<DatePeriod<T>> retVal = new ArrayList<>(periodicObjects.size());

        // если периоды пустые, значит дополнение - все множество возможных дат
        if (periodicObjects.size() == 0) {
            retVal.add(new DatePeriod<>(LocalDateTime.MIN, LocalDateTime.MAX, null));
            return retVal;
        }

        // включаем все то, что идет до начала наших периодов
        retVal.add(new DatePeriod<>(LocalDateTime.MIN, periodicObjects.get(0).getDateFrom(), null));

        // включаем все то, что идет между периодами
        for (int i = 0; i <= periodicObjects.size() - 2; i++) {

            DatePeriod<T> newPeriod = new DatePeriod<T>(periodicObjects.get(i).getDateTo(), periodicObjects.get(i + 1).getDateFrom(), null);

            if (!newPeriod.isEmptyOrNegative()) {
                retVal.add(newPeriod);
            }
        }

        // включаем все то, что идет просле окончания наших периодов
        retVal.add(new DatePeriod<>(periodicObjects.get(periodicObjects.size() - 1).getDateTo(), LocalDateTime.MAX, null));

        return retVal;
    }

    public static <T> int calculateTotalHourDuration(List<DatePeriod<T>> periodicObjects) {
        int totalHours = 0;
        for (DatePeriod<T> period : periodicObjects) {
            totalHours += period.getTotalHours();
        }
        return totalHours;
    }

    public static LocalDateTime maxDateTime(LocalDateTime d1, LocalDateTime d2) {
        return d1.isBefore(d2) ? d2 : d1;
    }

    public static LocalDateTime minDateTime(LocalDateTime d1, LocalDateTime d2) {
        return d1.isBefore(d2) ? d1 : d2;
    }
}
