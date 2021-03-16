package ru.audithon.egissostat.domain.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private Integer id;
    private Integer regionId;
    private Integer cityId;
    private Integer streetId;
    private String house;
    private String building;
    private String room;
    private String other;
    private String addressShortText;
    private String addressNormalized;
    private UUID houseguid;
    private UUID roomguid;
}



