package ru.audithon.egissostat.infrastructure.mass.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.infrastructure.mass.domain.*;
import ru.audithon.common.mapper.*;

import java.util.function.Function;

@Repository
@Transactional
public class JobScheduleItemDaoImpl extends PgCrudDaoBase<JobScheduleItem, Integer> implements JobScheduleItemDao {
    @Autowired
    public JobScheduleItemDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<JobScheduleItem, Integer>builder("job_schedule_item")
                .withFactory(JobScheduleItem::new)
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "id",
                    JobScheduleItem::getId, JobScheduleItem::setId, Function.identity(), true))
                .withColumn(ColumnMapper.of(Integer.class, "job_type_id",
                    JobScheduleItem::getJobTypeId, JobScheduleItem::setJobTypeId))
                .withColumn(ColumnMapper.of(String.class, "parameters",
                    JobScheduleItem::getParameters, JobScheduleItem::setParameters))
                .withColumn(ColumnMapper.of(Integer.class, "job_periodicity",
                    JobScheduleItem::getJobPeriodicity, JobScheduleItem::setJobPeriodicity))
                .withColumn(ColumnMapper.of(Integer.class, "allowed_hour_from",
                    JobScheduleItem::getAllowedHourFrom, JobScheduleItem::setAllowedHourFrom))
                .withColumn(ColumnMapper.of(Integer.class, "allowed_hour_to_excluded",
                    JobScheduleItem::getAllowedHourToExcluded, JobScheduleItem::setAllowedHourToExcluded))
                .withColumn(ColumnMapper.of(Integer.class, "planned_day",
                    JobScheduleItem::getPlannedDay, JobScheduleItem::setPlannedDay))
                .withColumn(ColumnMapper.of(Boolean.class, "enabled",
                    JobScheduleItem::getEnabled, JobScheduleItem::setEnabled))
                .build(),
            jdbcTemplate);
    }
}
