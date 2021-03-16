package ru.audithon.egissostat.domain.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityType {
    private @NotNull @Max(Short.MAX_VALUE) Integer id;
    private @NotNull @Length(max = 256) String caption;
    private @NotNull @Length(max = 256) String shortCaption;
}
