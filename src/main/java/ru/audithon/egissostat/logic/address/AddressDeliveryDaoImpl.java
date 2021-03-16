package ru.audithon.egissostat.logic.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.audithon.egissostat.domain.address.*;
import ru.audithon.egissostat.domain.common.DeliverySchema;
import ru.audithon.egissostat.logic.address.dto.AddressDeliveryPostWithRegion;
import ru.audithon.common.helpers.ObjectUtils;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.List;
import java.util.function.Function;


@Repository
@Transactional
public class AddressDeliveryDaoImpl extends PgCrudDaoBase<AddressDelivery, Integer> implements AddressDeliveryDao {

    private NamedParameterJdbcTemplate npJdbcTemplate;

    @Autowired
    public AddressDeliveryDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate npJdbcTemplate) {
        super(TableMapper.<AddressDelivery, Integer>builder("addr_delivery")
            .withFactory(AddressDelivery::new)
            .withKeyColumn(
                KeyColumnMapper.of(Integer.class, "id",
                        AddressDelivery::getId, AddressDelivery::setId, Function.identity(), true))
            .withColumn(ColumnMapper.of(Integer.class, "delivery_schema_id",
                    (AddressDelivery delivery) -> DeliverySchema.getNullableValue(delivery.getDeliverySchemaId()),
                    (AddressDelivery delivery, Integer value) -> delivery.setDeliverySchemaId(DeliverySchema.valueOf(value))))
            .withColumn(ColumnMapper.of(Integer.class, "region_id",
                        AddressDelivery::getRegionId, AddressDelivery::setRegionId))
            .withColumn(ColumnMapper.of(Integer.class, "city_id",
                    AddressDelivery::getCityId, AddressDelivery::setCityId))
            .withColumn(ColumnMapper.of(Integer.class, "street_id",
                    AddressDelivery::getStreetId, AddressDelivery::setStreetId))
            .withColumn(ColumnMapper.of(Integer.class, "house_from",
                    AddressDelivery::getHouseFrom, AddressDelivery::setHouseFrom))
            .withColumn(ColumnMapper.of(Integer.class, "house_to",
                    AddressDelivery::getHouseTo, AddressDelivery::setHouseTo))
            .withColumn(ColumnMapper.of(String.class, "house_concrete",
                    AddressDelivery::getHouseConcrete, AddressDelivery::setHouseConcrete))
            .withColumn(ColumnMapper.of(Integer.class, "house_parity",
                    AddressDelivery::getHouseParity, AddressDelivery::setHouseParity))
            .withColumn(ColumnMapper.of(Integer.class, "building_from",
                    AddressDelivery::getBuildingTo, AddressDelivery::setBuildingTo))
            .withColumn(ColumnMapper.of(Integer.class, "building_to",
                    AddressDelivery::getBuildingFrom, AddressDelivery::setBuildingFrom))
            .withColumn(ColumnMapper.of(String.class, "building_concrete",
                    AddressDelivery::getBuildingConcrete, AddressDelivery::setBuildingConcrete))
            .withColumn(ColumnMapper.of(Integer.class, "room_from",
                    AddressDelivery::getRoomFrom, AddressDelivery::setRoomFrom))
            .withColumn(ColumnMapper.of(Integer.class, "room_to",
                    AddressDelivery::getRoomTo, AddressDelivery::setRoomTo))
            .withColumn(ColumnMapper.of(Integer.class, "payreq_post_id",
                    AddressDelivery::getPayreqPostId, AddressDelivery::setPayreqPostId))
            .withColumn(ColumnMapper.of(Integer.class, "payreq_deliveryday",
                    AddressDelivery::getPayreqDeliveryDay, AddressDelivery::setPayreqDeliveryDay))
            .withColumn(ColumnMapper.of(Integer.class, "payreq_deliverybranch",
                    AddressDelivery::getPayreqDeliveryBranch, AddressDelivery::setPayreqDeliveryBranch))
            .build(), jdbcTemplate);

        this.npJdbcTemplate = npJdbcTemplate;
    }

    public AddressDelivery readLastByRoomHouseBuilding(DeliverySchema schema,
                                                       Integer regionId, Integer cityId, Integer streetId,
                                                       Integer houseNo, String building, Integer room){
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("houseNo", houseNo)
                .addValue("building", building)
                .addValue("room", room);

//        Object[] paramValues = new Object[1];

//        paramValues[0] = schema.getValue();
//        paramValues[1] = regionId;
//        paramValues[2] = cityId;
//        paramValues[3] = streetId;
//        paramValues[4] = houseNo;
//        paramValues[5] = houseNo;
//        paramValues[6] = building;
//        paramValues[7] = room;
//        paramValues[8] = room;

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND" +
                        "    (house_concrete = CAST(:houseNo AS VARCHAR(256))" +
                        "       OR" +
                        "     :houseNo BETWEEN house_from AND house_to)" +
                        "  AND coalesce(building_concrete, '') = :building" +
                        "  AND (room_from IS NULL OR room_from <= :room)" +
                        "  AND (room_to IS NULL OR :room <= room_to)" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByRoomConcreteHouse(DeliverySchema schema,
                                                       Integer regionId, Integer cityId, Integer streetId,
                                                       String house, Integer room){
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("house", house)
                .addValue("room", room);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_concrete = :house" +
                        "  AND (room_from <= :room)" +
                        "  AND (:room <= room_to)" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByConcreteBuildingConcreteHouse(DeliverySchema schema,
                                                                   Integer regionId, Integer cityId, Integer streetId,
                                                                   String house, String building){
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("house", house)
                .addValue("building", building);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_concrete = :house" +
                        "  AND building_concrete = :building" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByBuildingConcreteHouse(DeliverySchema schema,
                                                           Integer regionId, Integer cityId, Integer streetId,
                                                           String house, Integer building){
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("house", house)
                .addValue("building", building);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_concrete = :house" +
                        "  AND coalesce(building_from, 0) <= :building AND :building <= coalesce(building_to, 99999999)" +
                        "  AND (building_from is not null OR building_to is not null)" +
                        "  AND building_concrete is null" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByConcreteHouse(DeliverySchema schema,
                                                   Integer regionId, Integer cityId, Integer streetId,
                                                   String house){
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("house", house);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_concrete = :house" +
                        "  AND building_concrete is null" +
                        "  AND building_from is null" +
                        "  AND building_to is null" +
                        "  AND room_from is null" +
                        "  AND room_to is null" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByHouseRange(DeliverySchema schema,
                                                Integer regionId, Integer cityId, Integer streetId,
                                                Integer house){
        int houseParity = house != null ? house % 2 : 0;

        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId)
                .addValue("house", house)
                .addValue("houseParity", houseParity);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_from <= :house AND :house <= house_to" +
                        "  AND (house_parity is null OR house_parity = :houseParity)" +
                        "  AND house_concrete is null" +
                        "  AND building_concrete is null" +
                        "  AND building_from is null" +
                        "  AND building_to is null" +
                        "  AND room_from is null" +
                        "  AND room_to is null" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByStreet(DeliverySchema schema,
                                            Integer regionId, Integer cityId, Integer streetId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId)
                .addValue("streetId", streetId);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id = :streetId" +
                        "  AND house_from is null " +
                        "  AND house_to is null" +
                        "  AND house_concrete is null" +
                        "  AND building_concrete is null" +
                        "  AND building_from is null" +
                        "  AND building_to is null" +
                        "  AND room_from is null" +
                        "  AND room_to is null" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    public AddressDelivery readLastByCity(DeliverySchema schema,
                                          Integer regionId, Integer cityId) {
        SqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("deliverySchema", schema.getValue())
                .addValue("regionId", regionId)
                .addValue("cityId", cityId);

        return npJdbcTemplate.query(
                getSelectSql() +
                        " WHERE delivery_schema_id = :deliverySchema" +
                        "  AND region_id = :regionId" +
                        "  AND city_id = :cityId" +
                        "  AND street_id is null" +
                        "  AND house_from is null " +
                        "  AND house_to is null" +
                        "  AND house_concrete is null" +
                        "  AND building_concrete is null" +
                        "  AND building_from is null" +
                        "  AND building_to is null" +
                        "  AND room_from is null" +
                        "  AND room_to is null" +
                        " order by id desc " +
                        " limit 1"
                ,
                namedParameters,
                getMapper().getRowMapper()).stream().findFirst().orElse(null);
    }

    @Override
    public List<AddressDelivery> byRegionCityDelivery(Integer regionId, Integer cityId, DeliverySchema deliverySchemaId) {
        Assert.notNull(regionId, "regionId can't be null");
        Assert.notNull(cityId, "cityId can't be null");
        Assert.notNull(deliverySchemaId, "deliverySchemaId can't be null");

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("regionId", regionId);
        parameters.addValue("cityId", cityId);
        parameters.addValue("deliverySchemaId", deliverySchemaId.getValue());

        return npJdbcTemplate.query("select * from addr_delivery " +
                "where region_id = :regionId and city_id = :cityId and delivery_schema_id = :deliverySchemaId ",
            parameters, getMapper().getRowMapper());
    }

    @Override
    public List<AddressDeliveryPostWithRegion> getPostOfficesWithRegion() {
        return jdbcTemplate.query("SELECT DISTINCT ad.region_id, po.id, po.caption FROM addr_delivery ad " +
                        "JOIN postal_office po ON ad.payreq_post_id = po.id; ",
                new Object[] {}, (rs, rowNum) -> AddressDeliveryPostWithRegion.builder()
                        .regionId(rs.getInt("region_id"))
                        .id(rs.getInt("id"))
                        .caption(ObjectUtils.isNull(rs.getString("caption"), String.valueOf(rs.getInt("id"))))
                        .build());
    }

}
