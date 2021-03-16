package ru.audithon.egissostat.jobs.downloadegisso;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.audithon.egissostat.jobs.JobParameters;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DownloadEgissoStatisticParameters extends JobParameters {

    public static final String TYPE_NAME = "downloadEgissoStatistic";

    private LocalDate dateUpdate;

    @JsonIgnore
    public String getType() {
        return TYPE_NAME;
    }
}
