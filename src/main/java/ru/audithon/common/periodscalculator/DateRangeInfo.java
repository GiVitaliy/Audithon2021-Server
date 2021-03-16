package ru.audithon.common.periodscalculator;

import lombok.Data;
import lombok.Getter;
import ru.audithon.common.helpers.DateUtils;
import ru.audithon.common.types.DateRange;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.audithon.common.helpers.ObjectUtils.coalesce;

//@Data
public class DateRangeInfo implements Cloneable  {
    @Getter
    private DateRange dateRange;
    @Getter
    private final Map<Integer, Object> periodData = new HashMap<Integer, Object>(){};

    public DateRangeInfo(DateRange dateRange)
    {
        this.dateRange = dateRange;
    }

    @Override
    public DateRangeInfo clone() {
        DateRangeInfo clone = new DateRangeInfo((DateRange)dateRange.clone());

        for (Map.Entry<Integer, Object> entry : periodData.entrySet())
        {
            clone.periodData.put(entry.getKey(), entry.getValue());
        }

        return clone;
    }

    public LocalDate getMinDate()
    {
        return dateRange.getMinDate();
    }

    public LocalDate getMaxDate()
    {
        return dateRange.getMaxDate();
    }

    public LocalDate getMaxDateExcluded()
    {
        return dateRange.getMaxDateExcluded();
    }

    public void setDateFrom(LocalDate value)
    {
        dateRange = dateRange.cloneWithNewFrom(value);
    }

    public void setDateTo(LocalDate value)
    {
        dateRange = dateRange.cloneWithNewTo(value);
    }

    public void updateId(int id, Object data)
    {
        periodData.put(id, data);
    }

    public boolean hasEqualSigns(DateRangeInfo info)
    {
        return info.hasSigns(periodData) && hasSigns(info.periodData);
    }

    public <T> boolean hasSign(IdDataType id, T data) {
        return hasSign(id.getDataId(), data);
    }

    public boolean hasSign(int id, Object data)
    {
        Object val1 = data;
        Object val2 = periodData.get(id);

        return sameValues(val1, val2);
    }

    public static boolean sameValues(Object val1, Object val2)
    {
        if (val1 == null && val2 == null)
        {
            return true;
        }

        if(val1 == null || val2 == null)
        {
            return false;
        }

        if (val1 instanceof Integer && val2 instanceof Integer)
        {
            return (int) val1 == (int) val2;
        }
        if (val1 instanceof Boolean && val2 instanceof Boolean)
        {
            return (boolean)val1 == (boolean)val2;
        }
        if (val1 instanceof Double && val2 instanceof Double)
        {
            return Math.abs((double) val1 - (double) val2) < 10e-12;
        }
        if (val1 instanceof Float && val2 instanceof Float)
        {
            return Math.abs((float) val1 - (float) val2) < 10e-12;
        }

        if (val1 instanceof LocalDateTime && val2 instanceof LocalDateTime)
        {
            LocalDateTime date1 = (LocalDateTime)val1;
            LocalDateTime date2 = (LocalDateTime)val2;

            if ((date1.getHour() == 1 || date1.getHour() == 23) &&
                    date1.getMinute() == 0 && date1.getSecond() == 0 && date1.getNano() == 0)
            {
                date1 = date1.toLocalDate().atStartOfDay();
            }

            if ((date2.getHour() == 1 || date2.getHour() == 23) &&
                    date2.getMinute() == 0 && date2.getSecond() == 0 && date2.getNano() == 0)
            {
                date2 = date2.toLocalDate().atStartOfDay();
            }

            return date1.equals(date2);
        }

        return (val1).equals(val2);
        //return val1 == DBNull.Value && val2 == DBNull.Value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(IdDataType<T> id) {
        return (T)getSign(id.getDataId());
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(IdDataType<T> id, T defaultValue) {
        Object result = periodData.get(id.getDataId());
        return (T)coalesce(result, defaultValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int i)
    {
        return (T) getSign(i);
    }

    public Object getSign(int id)
    {
        return periodData.get(id);
    }

    public <T> boolean hasSignsTyped(Map<IdDataType<T>, T> signs) {
        return hasSigns(signs.entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getDataId(), Map.Entry::getValue)));
    }

    public boolean hasSigns(Map<Integer, Object> signs)
    {
        boolean retVal = true;

        for (Map.Entry<Integer, Object> p: signs.entrySet())
        {
            retVal = hasSign(p.getKey(), p.getValue());
            if (!retVal)
                break;
        }
        return retVal;
    }

    @Override
    public String toString()
    {
        return String.format("Период: %1$s - %2$s",
                DateUtils.formatRuDate(dateRange.getMinDate()), DateUtils.formatRuDate(dateRange.getMaxDate()));
    }
}
