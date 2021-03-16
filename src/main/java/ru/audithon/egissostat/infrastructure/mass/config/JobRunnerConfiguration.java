package ru.audithon.egissostat.infrastructure.mass.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Configuration
@ConfigurationProperties("job-runner")
@EnableAsync
@EnableScheduling
@Validated
public class JobRunnerConfiguration {
    @Min(1) private int dbWriterIntervalMs;
    @Min(1) private int corePoolSize = 5;
    @Min(1) private int maxPoolSize = 10;
    private int nodeId = 1;

    @Bean
    public TaskExecutor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setThreadNamePrefix("JOB-");
        executor.initialize();

        return executor;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getDbWriterIntervalMs() {
        return dbWriterIntervalMs;
    }

    public void setDbWriterIntervalMs(int dbWriterIntervalMs) {
        this.dbWriterIntervalMs = dbWriterIntervalMs;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
