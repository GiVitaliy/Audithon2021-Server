package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobKey {
    private final int typeId;
    private final int id;
}
