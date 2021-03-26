package ru.audithon.egissostat.domain.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorType {
    private @NotNull
    @Max(Short.MAX_VALUE) Integer id;
    private @NotNull @Length(max = 50) String code;
    private @NotNull @Length(max = 256) String caption;
    private String description;
    private Boolean favorite;
    private Boolean negative;
}
