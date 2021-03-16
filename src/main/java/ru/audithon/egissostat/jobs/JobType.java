package ru.audithon.egissostat.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.audithon.egissostat.jobs.downloadegisso.DownloadEgissoStatisticParameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobType {
    private int id;
    private String caption;
    private String runnerBeanName;
    private Boolean isVolatile;

    private static ArrayList<JobType> list;
    private static Map<String, JobType> map;
    private static Map<Integer, JobType> map2;
    private static final Integer syncroot = 1;

    public static List<JobType> getConstantDictionaryContent() {

        synchronized (syncroot) {

            if (list == null) {
                list = new ArrayList<>();
                list.add(new JobType(1, "Актуализация статистики из ЕГИССО", DownloadEgissoStatisticParameters.TYPE_NAME, false));
            }
            return list;
        }
    }

    public static Map<String, JobType> getConstantDictionaryContentMap() {

        synchronized (syncroot) {

            if (map == null) {
                map = new HashMap<>();
                getConstantDictionaryContent().forEach(jt -> map.put(jt.runnerBeanName, jt));
            }

            return map;
        }
    }

    public static Map<Integer, JobType> getConstantDictionaryContentMap2() {

        synchronized (syncroot) {

            if (map2 == null) {
                map2 = new HashMap<>();
                getConstantDictionaryContent().forEach(jt -> map2.put(jt.getId(), jt));
            }

            return map2;
        }
    }

}
