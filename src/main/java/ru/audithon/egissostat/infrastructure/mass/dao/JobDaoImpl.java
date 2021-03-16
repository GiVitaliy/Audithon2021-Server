package ru.audithon.egissostat.infrastructure.mass.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.audithon.egissostat.infrastructure.mass.domain.*;
import ru.audithon.common.mapper.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.audithon.common.mapper.QueryExpression.*;

@Repository
@Transactional
public class JobDaoImpl extends PgCrudDaoBase<Job, JobKey> implements JobDao {

    private static final Logger logger = LoggerFactory.getLogger(JobDaoImpl.class);

    @Autowired
    public JobDaoImpl(JdbcTemplate jdbcTemplate) {
        super(TableMapper.<Job, JobKey>builder("job")
                .withFactory(Job::new)
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "job_type_id",
                    Job::getTypeId, Job::setTypeId, JobKey::getTypeId))
                .withKeyColumn(KeyColumnMapper.of(Integer.class, "id",
                    Job::getId, Job::setId, JobKey::getId, true))
                .withColumn(ColumnMapper.of(Integer.class, "job_state_id",
                    Job::getStateId, Job::setStateId))
                .withColumn(ColumnMapper.of(LocalDateTime.class, "created",
                    Job::getCreated, Job::setCreated))
                .withColumn(ColumnMapper.of(LocalDateTime.class, "completed",
                    Job::getCompleted, Job::setCompleted))
                .withColumn(ColumnMapper.of(LocalDateTime.class, "heartbeat",
                    Job::getHeartbeat, Job::setHeartbeat))
                .withColumn(ColumnMapper.of(Integer.class, "progress",
                    Job::getProgress, Job::setProgress))
                .withColumn(ColumnMapper.of(Integer.class, "node_id",
                    Job::getNodeId, Job::setNodeId))
                .withColumn(ColumnMapper.of(String.class, "message",
                    Job::getMessage, Job::setMessage))
                .withColumn(ColumnMapper.of(String.class, "parameters",
                    Job::getParameters, Job::setParameters))
                .withColumn(ColumnMapper.of(String.class, "result",
                    Job::getResult, Job::setResult))
                .withColumn(ColumnMapper.of(Integer.class, "user_id",
                    Job::getUserId, Job::setUserId))
                .withColumn(ColumnMapper.of(String.class, "digest",
                    Job::getDigest, Job::setDigest))
                .build(),
            jdbcTemplate);
    }

    @Override
    public List<Job> byDigestCompleted(String digest) {
        return jdbcTemplate.query(getSelectSql() + " where digest = ? and job_state_id = ? " +
                "order by completed desc LIMIT 10",
            new Object[]{digest, JobState.COMPLETED},
            getMapper().getRowMapper());
    }

    public List<Job> byDigestLatest10(String digest) {
        return jdbcTemplate.query(getSelectSql() + " where digest = ? order by created desc LIMIT 10",
            new Object[]{digest},
            getMapper().getRowMapper());
    }

    @Override
    public List<Job> byStateAndNode(int nodeId, int stateId) {
        return jdbcTemplate.query(getSelectSql() + " where node_id = ? and job_state_id = ?",
            new Object[]{nodeId, stateId},
            getMapper().getRowMapper());
    }

    @Override
    public List<Job> byStateAndCreatedDateRange(Integer stateId, LocalDate dateFrom, LocalDate dateTo, Integer userId) {
        QueryBuilder qb = new QueryBuilder()
            .add(eq("job_state_id", stateId))
            .add(get("created", dateFrom))
            .add(let("created", dateTo));

        if (userId != null) {
            qb.add(eq("user_id", userId));
        }

        qb.removeNullValues();

        return jdbcTemplate.query(getSelectSql() + qb.getWhereExpression() + "\norder by id desc",
            qb.getValues(),
            getMapper().getRowMapper());
    }

    @Override
    public int changeStates(int nodeId, int stateFrom, int stateTo) {
        return changeStates(nodeId, stateFrom, stateTo, null);
    }

    @Override
    public int changeStates(int nodeId, int stateFrom, int stateTo, String message) {
        return jdbcTemplate.update("UPDATE job SET job_state_id = ?, message = ? " +
                " WHERE node_id = ? AND job_state_id = ?",
            stateTo, message, nodeId, stateFrom);
    }

    @Override
    public int updateProgress(JobProgress progress) {
        Objects.requireNonNull(progress);
        Objects.requireNonNull(progress.getJobKey());

        return jdbcTemplate.update("UPDATE job SET progress = ?, heartbeat = ?,  job_state_id = ?" +
                " WHERE job_type_id = ? AND id = ?",
            progress.getProgress(),
            progress.getTimestamp(),
            JobState.RUNNING,
            progress.getJobKey().getTypeId(),
            progress.getJobKey().getId());
    }

    @Override
    public int setCompleted(JobProgress progress) {
        Optional<Job> job = byId(progress.getJobKey());
        return job.map(j -> {
            j.setStateId(JobState.COMPLETED);
            j.setCompleted(LocalDateTime.now());
            j.setHeartbeat(progress.getTimestamp());
            j.setProgress(progress.getProgress());
            j.setResult(progress.getResult());

            logger.debug("job setCompleted() update called");
            return update(j, j.getKey());
        })
            .orElse(0);
    }

    @Override
    public int setState(JobKey key, int state, String message) {
        Optional<Job> job = byId(key);
        return job.map(j -> {
            j.setStateId(state);
            j.setMessage(message);
            return update(j, j.getKey());
        })
                .orElse(0);
    }

    @Override
    public int setFailed(JobKey key, String message) {
        return setState(key, JobState.FAILED, message);
    }

    @Override
    public int setCanceled(JobKey key, String message) {
        return setState(key, JobState.CANCELED, message);
    }

    @Override
    public int setCanceled(JobKey key, String message, String result) {
        Optional<Job> job = byId(key);
        return job.map(j -> {
            j.setStateId(JobState.CANCELED);
            j.setMessage(message);
            j.setResult(result);
            return update(j, j.getKey());
        })
                .orElse(0);
    }
}
