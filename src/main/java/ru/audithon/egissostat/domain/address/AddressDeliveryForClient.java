package ru.audithon.egissostat.domain.address;

import lombok.*;
import org.springframework.beans.BeanUtils;
import ru.audithon.egissostat.domain.common.DeliverySchema;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AddressDeliveryForClient extends AddressDelivery {
    private Boolean isNew;
    private Boolean isModified;
    private Integer id;

    @Builder(builderMethodName = "childBuilder")
    public AddressDeliveryForClient(Integer id,
                                    DeliverySchema deliverySchemaId,
                                    Integer regionId,
                                    Integer cityId,
                                    Integer streetId,
                                    Integer houseFrom,
                                    Integer houseTo,
                                    String houseConcrete,
                                    Integer buildingFrom,
                                    Integer buildingTo,
                                    String buildingConcrete,
                                    Integer payreqPostId,
                                    Integer payreqDeliveryDay,
                                    Integer payreqDeliveryBranch,
                                    Integer houseParity,
                                    Integer roomFrom,
                                    Integer roomTo,
                                    Boolean isNew, Boolean isModified) {
        super(id, deliverySchemaId, regionId, cityId, streetId, houseFrom, houseTo, houseConcrete, buildingFrom, buildingTo,
            buildingConcrete, payreqPostId, payreqDeliveryDay, payreqDeliveryBranch, houseParity, roomFrom, roomTo);

        this.isNew = isNew;
        this.isModified = isModified;
    }

    public static AddressDeliveryForClient ofAddressDelivery(AddressDelivery addressDelivery) {
        AddressDeliveryForClient target = new AddressDeliveryForClient();
        BeanUtils.copyProperties(addressDelivery, target);
        target.setIsModified(false);
        target.setIsNew(false);
        target.setId(addressDelivery.getId());
        return target;
    }


}
