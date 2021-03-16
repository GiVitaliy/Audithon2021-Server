package ru.audithon.egissostat.logic.address;

import ru.audithon.egissostat.domain.address.Address;
import ru.audithon.egissostat.domain.address.AddressFull;
import ru.audithon.common.mapper.CrudDao;

import java.util.List;
import java.util.Optional;

public interface AddressDao extends CrudDao<Address, Integer> {
    AddressFull getAddressFull(int id);
    Optional<Integer> findId(Address address);
    Integer ensureExists(Address address);
    List<Address> readFromAddressDocByPersonId(Integer id);
    List<Address> readFromZkhDocByPersonId(Integer id);
    boolean tryUpdateCalculatedFields(Address address);
}
