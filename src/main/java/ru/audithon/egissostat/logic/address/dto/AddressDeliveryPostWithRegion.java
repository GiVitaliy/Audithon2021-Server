package ru.audithon.egissostat.logic.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDeliveryPostWithRegion {
    private Integer regionId;
    private Integer id;
    private String caption;
}
