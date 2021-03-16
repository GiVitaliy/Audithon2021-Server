package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobState {
    private int id;
    private String caption;

    public final static int NEW = 1;
    public final static int RUNNING = 2;
    public final static int COMPLETED = 3;
    public final static int CANCELED = 4;
    public final static int FAILED = 5;
    public final static int TERMINATED = 6;
}
