package ru.audithon.egissostat.infrastructure.mass.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class JobScheduleItemPeriodicity {

    private Integer id;
    private String caption;

    public static final int HOURLY = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;
    public static final int QURTERLY = 5;
    public static final int YEARLY = 6;

    public static List<JobScheduleItemPeriodicity> getConstantDictionaryContent() {
        ArrayList<JobScheduleItemPeriodicity> list = new ArrayList<>();
        list.add(new JobScheduleItemPeriodicity(HOURLY, "Ежечасно"));
        list.add(new JobScheduleItemPeriodicity(DAILY, "Ежедневно"));
        list.add(new JobScheduleItemPeriodicity(WEEKLY, "Еженедельно"));
        list.add(new JobScheduleItemPeriodicity(MONTHLY, "Ежемесячно"));
        list.add(new JobScheduleItemPeriodicity(QURTERLY, "Ежеквартально"));
        list.add(new JobScheduleItemPeriodicity(YEARLY, "Ежегодно"));
        return list;
    }}
