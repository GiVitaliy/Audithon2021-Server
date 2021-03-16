package ru.audithon.egissostat.filestorage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PreloadedFileData {
    private InputStream content;
    private String uri;
    private String prettyFileName;
    private LocalDateTime createdTime;
}
