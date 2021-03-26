package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Indicator {
    private Integer indicatorTypeId; // class IndicatorType
    private Integer stateId;
    private Integer year;
    private Integer month;
    private BigDecimal value;
    private BigDecimal valueMa;
    private BigDecimal valueMaTrend;
    private BigDecimal valueMaTrendX2;

    @Data
    @AllArgsConstructor
    public static class Key {
        private Integer indicatorTypeId; // class IndicatorType
        private Integer stateId;
        private Integer year;
        private Integer month;
    }

    public Key getKey() {
        return new Key(indicatorTypeId, stateId, year, month);
    }
}
