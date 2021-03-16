package ru.audithon.egissostat.domain.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.audithon.egissostat.domain.common.DeliverySchema;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegionCitySchemaAddressDelivery {
    private Integer regionId;
    private Integer cityId;
    private DeliverySchema deliverySchemaId;
    private List<AddressDeliveryForClient> delivery;
}
