package ru.audithon.egissostat.domain.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {
    private @NotNull @Max(Short.MAX_VALUE) Integer regionId;
    private @Max(Short.MAX_VALUE) Integer id;
    private @NotNull @Length(max = 256) String caption;
    private @NotNull @Max(Short.MAX_VALUE) Integer type;
    private @NotNull Boolean isDefault;
    private @NotNull BigDecimal rk;
    private @Length(max = 256) String codePfr62;
    private UUID aoguid;

    @JsonIgnore
    public CityKey getKey() {
        return new CityKey(regionId, id);
    }

    @Data
    @AllArgsConstructor
    public static class CityKey {
        private @NotNull @Max(Short.MAX_VALUE) Integer regionId;
        private @NotNull @Max(Short.MAX_VALUE) Integer id;
    }
}
