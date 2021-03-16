package ru.audithon.common.periodscalculator;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.audithon.common.types.DateRange;

@ToString
@EqualsAndHashCode
public class PayloadDateRange<T> {
    @Getter
    private final DateRange dateRange;
    @Getter
    private final T payload;

    public PayloadDateRange(DateRange dateRange, T payload) {
        this.dateRange = dateRange;
        this.payload = payload;
    }
}
