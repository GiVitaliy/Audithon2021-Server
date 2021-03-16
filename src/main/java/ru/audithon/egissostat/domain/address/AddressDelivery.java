package ru.audithon.egissostat.domain.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import ru.audithon.egissostat.domain.common.DeliverySchema;
import ru.audithon.common.helpers.StringUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDelivery {
    @NotNull
    private Integer id;
    @NotNull
    private DeliverySchema deliverySchemaId;
    @NotNull
    @Max(Short.MAX_VALUE)
    private Integer regionId;
    @NotNull
    @Max(Short.MAX_VALUE)
    private Integer cityId;
    @Max(Short.MAX_VALUE)
    private Integer streetId;
    private Integer houseFrom;
    private Integer houseTo;
    @Length(max = 256)
    private String houseConcrete;
    private Integer buildingFrom;
    private Integer buildingTo;
    @Length(max = 256)
    private String buildingConcrete;
    private Integer payreqPostId;
    @NotNull
    @Max(Short.MAX_VALUE)
    private Integer payreqDeliveryDay;
    @NotNull
    @Max(Short.MAX_VALUE)
    private Integer payreqDeliveryBranch;
    private Integer houseParity;
    private Integer roomFrom;
    private Integer roomTo;

    public void prettify() {
        houseConcrete = StringUtils.prettify(houseConcrete);
        buildingConcrete = StringUtils.prettify(buildingConcrete);
    }
}
