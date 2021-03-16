package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobScheduleItem {
    private Integer id;
    private Integer jobTypeId;
    private String parameters;
    private Integer jobPeriodicity; // JobScheduleItemPeriodicity
    private Integer allowedHourFrom;
    private Integer allowedHourToExcluded;
    private Integer plannedDay;
    private Boolean enabled;
}
