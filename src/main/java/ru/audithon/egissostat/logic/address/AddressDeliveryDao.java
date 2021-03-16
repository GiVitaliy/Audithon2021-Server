package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.Address;
import ru.audithon.egissostat.domain.address.AddressDelivery;
import ru.audithon.egissostat.domain.common.DeliverySchema;
import ru.audithon.egissostat.logic.address.dto.AddressDeliveryPostWithRegion;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.Optional;

public interface AddressDeliveryDao extends CrudDao<AddressDelivery, Integer> {
    AddressDelivery readLastByRoomHouseBuilding(DeliverySchema schema,
                                                Integer regionId, Integer cityId, Integer streetId,
                                                Integer houseNo, String building, Integer room);
    AddressDelivery readLastByRoomConcreteHouse(DeliverySchema schema,
                                                Integer regionId, Integer cityId, Integer streetId,
                                                String house, Integer room);
    AddressDelivery readLastByConcreteBuildingConcreteHouse(DeliverySchema schema,
                                                            Integer regionId, Integer cityId, Integer streetId,
                                                            String house, String building);
    AddressDelivery readLastByBuildingConcreteHouse(DeliverySchema schema,
                                                            Integer regionId, Integer cityId, Integer streetId,
                                                            String house, Integer building);
    AddressDelivery readLastByConcreteHouse(DeliverySchema schema,
                                                    Integer regionId, Integer cityId, Integer streetId,
                                                    String house);
    AddressDelivery readLastByHouseRange(DeliverySchema schema,
                                            Integer regionId, Integer cityId, Integer streetId,
                                            Integer house);
    AddressDelivery readLastByStreet(DeliverySchema schema,
                                         Integer regionId, Integer cityId, Integer streetId);
    AddressDelivery readLastByCity(DeliverySchema schema,
                                     Integer regionId, Integer cityId);

    List<AddressDelivery> byRegionCityDelivery(Integer regionId, Integer cityId, DeliverySchema deliverySchemaId);
    List<AddressDeliveryPostWithRegion> getPostOfficesWithRegion();
}
