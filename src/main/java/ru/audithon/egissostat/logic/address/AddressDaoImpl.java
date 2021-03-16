package ru.audithon.egissostat.logic.address;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.helpers.StringUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;
import ru.audithon.egissostat.domain.address.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
@Transactional
public class AddressDaoImpl extends PgCrudDaoBase<Address, Integer> implements AddressDao {

    @Autowired
    public AddressDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<Address, Integer>builder("address")
            .withFactory(Address::new)
            .withKeyColumn(
                KeyColumnMapper.of(Integer.class, "id",
                    Address::getId, Address::setId, Function.identity(), true))
            .withColumn(ColumnMapper.of(Integer.class, "region_id",
                Address::getRegionId, Address::setRegionId))
            .withColumn(ColumnMapper.of(Integer.class, "city_id",
                Address::getCityId, Address::setCityId))
            .withColumn(ColumnMapper.of(Integer.class, "street_id",
                Address::getStreetId, Address::setStreetId))
            .withColumn(ColumnMapper.of(String.class, "house",
                Address::getHouse, Address::setHouse))
            .withColumn(ColumnMapper.of(String.class, "building",
                Address::getBuilding, Address::setBuilding))
            .withColumn(ColumnMapper.of(String.class, "room",
                Address::getRoom, Address::setRoom))
            .withColumn(ColumnMapper.of(String.class, "other",
                Address::getOther, Address::setOther))
            .withColumn(ColumnMapper.of(String.class, "address_short_text",
                Address::getAddressShortText, Address::setAddressShortText))
            .withColumn(ColumnMapper.of(String.class, "address_normalized",
                Address::getAddressNormalized, Address::setAddressNormalized))
            .withColumn(ColumnMapper.of(UUID.class, "houseguid",
                Address::getHouseguid, Address::setHouseguid))
            .withColumn(ColumnMapper.of(UUID.class, "roomguid",
                Address::getRoomguid, Address::setRoomguid))
            .build(), jdbcTemplate);
    }

    @Override
    public AddressFull getAddressFull(int id) {
        return jdbcTemplate.queryForObject(
            " select a.other, a.house, a.building, a.room, " +
                "        c.caption, c.is_default, ct.short_caption, " +
                "        st.caption, stt.short_caption, rg.caption " +
                " from    address a " +
                " LEFT JOIN addr_region rg ON rg.id = a.region_id " +
                " left join addr_city c on a.region_id = c.region_id and a.city_id = c.id " +
                " left join addr_street st on a.region_id = st.region_id and a.city_id = st.city_id and a.street_id = st.id " +
                " left join addr_street_type stt on st.type = stt.id " +
                " left join addr_city_type ct on c.type = ct.id " +
                " where a.id = ?",
            new Object[] {id},
            (rs, rowNum) -> AddressFull.builder()
                .address(Address.builder()
                    .other(rs.getString(1))
                    .house(rs.getString(2))
                    .building(rs.getString(3))
                    .room(rs.getString(4))
                    .build())
                .city(City.builder()
                    .caption(rs.getString(5))
                    .isDefault(rs.getBoolean(6))
                    .build())
                .cityType(new CityType(null, null, rs.getString(7)))
                .street(Street.builder()
                    .caption(rs.getString(8)).build())
                .streetType(new StreetType(null, null, rs.getString(9)))
                .region(new Region(null, rs.getString(10), null, null))
                .build()
        );
    }

    @Override
    public Optional<Integer> findId(Address address) {
        Objects.requireNonNull(address);

        if (Strings.isNullOrEmpty(address.getOther())) {
            if (Strings.isNullOrEmpty(address.getRoom())) {
                return findWithoutRoom(address);
            }

            return findWithRoom(address);
        }

        return findByOther(address.getOther());
    }

    @Override
    public Integer ensureExists(Address address) {
        Optional<Integer> existing = findId(address);
        if (existing.isPresent()) {
            return existing.get();
        }

        Address newOne = insert(address);

        tryUpdateCalculatedFields(newOne);

        return newOne.getId();
    }

    @Override
    public List<Address> readFromAddressDocByPersonId(Integer id) {
        return jdbcTemplate.query(getSelectSql() + " WHERE id in (select d.t1_regaddress from document d where d.person_id = ?)",
                new Object[]{id}, getMapper().getRowMapper());
    }

    @Override
    public List<Address> readFromZkhDocByPersonId(Integer id) {
        return jdbcTemplate.query(getSelectSql() + " WHERE id in (select d.address_id from doc_zkh_service d where d.person_id = ?)",
                new Object[]{id}, getMapper().getRowMapper());
    }

    private Optional<Integer> findByOther(String other) {
        try {
            Integer value = jdbcTemplate.queryForObject(
                " select id from address where other = ?",
                new Object[]{other},
                Integer.class);

            return Optional.of(value);

        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Integer> findWithRoom(Address address) {
        try {
            Integer value = jdbcTemplate.queryForObject(
                " select id from address " +
                    " where region_id = ? " +
                    "   and city_id = ? " +
                    "   and street_id = ? " +
                    "   and house = ?  " +
                    "   and coalesce(building, '') = ? " +
                    "   and room = ?  " +
                    "limit 1",
                new Object[]{
                    address.getRegionId(),
                    address.getCityId(),
                    address.getStreetId(),
                    address.getHouse(),
                    address.getBuilding() == null ? "" : address.getBuilding(),
                    address.getRoom()
                },
                Integer.class);

            return Optional.of(value);

        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Integer> findWithoutRoom(Address address) {
        try {
            Integer value = jdbcTemplate.queryForObject(
                " select id from address a " +
                    " where region_id = ? " +
                    "   and city_id = ? " +
                    "   and street_id = ? " +
                    "   and house = ?  " +
                    "   and coalesce(building, '') = ? " +
                    "   and room is null " +
                    "limit 1",
                new Object[]{
                    address.getRegionId(),
                    address.getCityId(),
                    address.getStreetId(),
                    address.getHouse(),
                    address.getBuilding() == null ? "" : address.getBuilding()
                },
                Integer.class);

            return Optional.of(value);

        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public boolean tryUpdateCalculatedFields(Address address) {

        Address currentAddress = byId(address.getId())
            .orElseThrow(() -> new BusinessLogicException(null, "a18main.Common.objectNotFound"));

        AddressFull addressFull = getAddressFull(address.getId());

        address.setAddressShortText(addressFull.getShortCaption(true));
        address.setAddressNormalized(StringUtils.normalizeAddress(addressFull.getShortCaption(false)));

        if (Objects.equals(currentAddress.getAddressShortText(), address.getAddressShortText()) &&
            Objects.equals(currentAddress.getAddressNormalized(), address.getAddressNormalized())) {
            return false;
        }

        update(address, address.getId());

        return true;
    }
}
