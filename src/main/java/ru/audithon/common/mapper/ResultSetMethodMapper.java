package ru.audithon.common.mapper;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.UUID;

public class ResultSetMethodMapper {
    public final ImmutableMap<Class<?>, SqlBiFunction<ResultSet, Integer, ?>> mapper;
    public final ImmutableMap<Class<?>, SqlBiFunction<ResultSet, String, ?>> labelMapper;

    public ResultSetMethodMapper() {
        HashMap<Class<?>, SqlBiFunction<ResultSet, Integer, ?>> mapper = new HashMap<>();

        mapper.put(String.class, ResultSet::getString);
        mapper.put(Boolean.class, RsUtils::getNullableBoolean);
        mapper.put(BigDecimal.class, ResultSet::getBigDecimal);
        mapper.put(Byte.class, RsUtils::getNullableShort);
        mapper.put(Double.class, RsUtils::getNullableDouble);
        mapper.put(Float.class, RsUtils::getNullableFloat);
        mapper.put(Integer.class, RsUtils::getNullableInt);
        mapper.put(Long.class, RsUtils::getNullableLong);
        mapper.put(Short.class, RsUtils::getNullableShort);
        mapper.put(LocalDate.class, RsUtils::getLocalDate);
        mapper.put(LocalDateTime.class, RsUtils::getLocalDateTime);
        mapper.put(OffsetDateTime.class, RsUtils::getOffsetDateTime);
        mapper.put(LocalTime.class, RsUtils::getLocalTime);
        mapper.put(UUID.class, RsUtils::getUuid);


        this.mapper = ImmutableMap.copyOf(mapper);

        HashMap<Class<?>, SqlBiFunction<ResultSet, String, ?>> labelMapper = new HashMap<>();

        labelMapper.put(String.class, ResultSet::getString);
        labelMapper.put(Boolean.class, RsUtils::getNullableBoolean);
        labelMapper.put(BigDecimal.class, ResultSet::getBigDecimal);
        labelMapper.put(Byte.class, RsUtils::getNullableShort);
        labelMapper.put(Double.class, RsUtils::getNullableDouble);
        labelMapper.put(Float.class, RsUtils::getNullableFloat);
        labelMapper.put(Integer.class, RsUtils::getNullableInt);
        labelMapper.put(Long.class, RsUtils::getNullableLong);
        labelMapper.put(Short.class, RsUtils::getNullableShort);
        labelMapper.put(LocalDate.class, RsUtils::getLocalDate);
        labelMapper.put(LocalDateTime.class, RsUtils::getLocalDateTime);
        labelMapper.put(OffsetDateTime.class, RsUtils::getOffsetDateTime);
        labelMapper.put(LocalTime.class, RsUtils::getLocalTime);
        labelMapper.put(UUID.class, RsUtils::getUuid);

        this.labelMapper = ImmutableMap.copyOf(labelMapper);
    }
}
