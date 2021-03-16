package ru.audithon.egissostat.logic.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.domain.user.UserSetting;
import ru.audithon.egissostat.domain.user.UserSettingUi;
import ru.audithon.common.mapper.*;

import java.util.function.Function;

@Repository
@Transactional
public class UserSettingDaoImpl extends PgCrudDaoBase<UserSetting, Integer> implements UserSettingDao {

    @Autowired
    public UserSettingDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<UserSetting, Integer>builder("user_setting")
                .withFactory(UserSetting::new)
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "unique_user_id",
                        UserSetting::getUniqueUserId, UserSetting::setUniqueUserId, Function.identity()))
                .withColumn(ColumnMapper.of(String.class, "ui",
                        JsonMappers.ofValue(UserSetting::getUserSettingUi), JsonMappers.ofValue(UserSetting::setUserSettingUi, UserSettingUi.class)))
                .build(), jdbcTemplate);
    }
}
