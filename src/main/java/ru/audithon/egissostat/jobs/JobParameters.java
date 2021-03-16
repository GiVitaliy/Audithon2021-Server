package ru.audithon.egissostat.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import ru.audithon.egissostat.jobs.downloadegisso.DownloadEgissoStatisticParameters;

import java.io.InputStream;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DownloadEgissoStatisticParameters.class, name = DownloadEgissoStatisticParameters.TYPE_NAME),
})
@Data
public abstract class JobParameters {
    @JsonIgnore
    private InputStream uploadedFileStream;
    @JsonIgnore
    public abstract String getType();
}
