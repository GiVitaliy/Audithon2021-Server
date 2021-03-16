package ru.audithon.egissostat.infrastructure.mass.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.infrastructure.mass.domain.JobState;
import ru.audithon.common.mapper.ColumnMapper;
import ru.audithon.common.mapper.KeyColumnMapper;
import ru.audithon.common.mapper.PgCrudDaoBase;
import ru.audithon.common.mapper.TableMapper;

import java.util.function.Function;

@Repository
@Transactional
public class JobStateDaoImpl extends PgCrudDaoBase<JobState, Integer> implements JobStateDao {

    @Autowired
    public JobStateDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<JobState, Integer>builder("job_state")
            .withFactory(JobState::new)
            .withKeyColumn(KeyColumnMapper.of(Integer.class, "id",
                JobState::getId, JobState::setId, Function.identity()))
            .withColumn(ColumnMapper.of(String.class, "caption",
                JobState::getCaption, JobState::setCaption))
            .build(),
            jdbcTemplate);
    }
}
