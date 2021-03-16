package ru.audithon.egissostat.infrastructure.mass.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    private int typeId;
    private int id;
    private Integer userId;

    private int stateId;
    private LocalDateTime created;
    private LocalDateTime completed;
    private LocalDateTime heartbeat;
    private int progress;

    private int nodeId;

    private String message;
    private String parameters;
    private String result;
    private String digest;

    @JsonIgnore
    public JobKey getKey() {
        return new JobKey(typeId, id);
    }
}
