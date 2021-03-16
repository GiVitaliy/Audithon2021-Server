package ru.audithon.egissostat.domain.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Street {
    private @NotNull @Max(Short.MAX_VALUE) Integer regionId;
    private @NotNull @Max(Short.MAX_VALUE) Integer cityId;
    private @Max(Short.MAX_VALUE) Integer id;
    private @NotNull @Length(max = 256) String caption;
    private @NotNull @Max(Short.MAX_VALUE) Integer type;
    private @Length(max = 256) String codePfr62;
    private UUID aoguid;

    @JsonIgnore
    public StreetKey getKey() {
        return new StreetKey(regionId, cityId, id);
    }

    @Data
    @AllArgsConstructor
    public static class StreetKey {
        private @NotNull @Max(Short.MAX_VALUE) Integer regionId;
        private @NotNull @Max(Short.MAX_VALUE) Integer cityId;
        private @NotNull @Max(Short.MAX_VALUE) Integer id;
    }
}
