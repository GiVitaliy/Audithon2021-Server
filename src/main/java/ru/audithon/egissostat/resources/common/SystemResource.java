package ru.audithon.egissostat.resources.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.rolling.RollingFileAppender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.audithon.common.exceptions.FileNameTooLongException;
import ru.audithon.common.helpers.DateUtils;
import ru.audithon.egissostat.infrastructure.TelemetryService;
import ru.audithon.egissostat.filestorage.FileStorage;
import ru.audithon.egissostat.filestorage.TempFileInfo;
import ru.audithon.egissostat.resources.ApiResultDto;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping(value = "/system",
    produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemResource {
    private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);

    private final FileStorage fileStorage;
    private final TelemetryService telemetryService;

    @Autowired
    public SystemResource(FileStorage fileStorage,
                          TelemetryService telemetryService) {
        this.fileStorage = fileStorage;
        this.telemetryService = telemetryService;
    }

    // Загрузка файлов происходит двухстадийно. С помощью этого метода, который доступен толкьо администратору,
    // происходит формирование архива с лог-файлами и возвращение на клиент уникального идентификатора для скачки через /files/get
    @PostMapping(path = "/prepare-log", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResultDto> prepareFile(@RequestBody LogRequestParams params) throws IOException, FileNameTooLongException {

        Set<File> logFiles = getLogFiles(params);

        TempFileInfo tempFileInfo = fileStorage.zipFiles(
            String.format("logs-%s.zip", DateUtils.formatRuDateTime(LocalDateTime.now()).replace(":", "-")),
            logFiles);

        InputStream inputStream = fileStorage.readFile(tempFileInfo.getUri(), true);
        String oneTimeDownLoadLink = fileStorage.generateOneTimeDownloadLink(inputStream, tempFileInfo.getUri());

        return new ResponseEntity<>(
            new ApiResultDto(new ArrayList<>(), oneTimeDownLoadLink),
            HttpStatus.OK
        );
    }

    private Set<File> getLogFiles(@RequestBody LogRequestParams params) {
        Set<String> logFolders = new HashSet<>();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(Logger.ROOT_LOGGER_NAME).iteratorForAppenders().forEachRemaining(appender -> {
            if (appender instanceof RollingFileAppender) {
                RollingFileAppender fileAppender = (RollingFileAppender) appender;
                File currentFile = new File(fileAppender.getFile());
                logFolders.add(currentFile.getParent());
            }
        });

        Set<File> logFiles = new HashSet<>();

        logFolders.forEach(folderPath -> {
            File folder = new File(folderPath);

            FileFilter filter = pathname -> {
                try {
                    BasicFileAttributes attr = Files.readAttributes(Paths.get(pathname.getPath()), BasicFileAttributes.class);
                    if (!attr.isRegularFile()) {
                        return false;
                    }

                    ZonedDateTime zonedDateTimeTo = ZonedDateTime.of(params.dateTo, ZoneId.systemDefault());
                    if (attr.creationTime().toInstant().isAfter(Instant.from(zonedDateTimeTo))) {
                        return false;
                    }

                    ZonedDateTime zonedDateTimeFrom = ZonedDateTime.of(params.dateFrom, ZoneId.systemDefault());
                    if (attr.lastModifiedTime().toInstant().isBefore(Instant.from(zonedDateTimeFrom))) {
                        return false;
                    }

                } catch (IOException ex) {
                    logger.info(String.format("Не удалось получить информацию о файле %s", pathname.getAbsolutePath()), ex);
                    return false;
                }

                return true;
            };

            logFiles.addAll(Arrays.asList(Objects.requireNonNull(folder.listFiles(filter))));
        });
        return logFiles;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogRequestParams {
        private LocalDateTime dateFrom;
        private LocalDateTime dateTo;
    }

    @GetMapping(path = "/get-gathered-operations")
    public ResponseEntity<ApiResultDto> getGatheredOperations() {
        return new ResponseEntity<>(
            new ApiResultDto(new ArrayList<>(), telemetryService.getGatheredOperations()),
            HttpStatus.OK
        );
    }

    @GetMapping(path = "/get-running-operations")
    public ResponseEntity<ApiResultDto> getRunningOperations() {
        return new ResponseEntity<>(
            new ApiResultDto(new ArrayList<>(), telemetryService.getRunningOperations()),
            HttpStatus.OK
        );
    }

    @Data
    @NoArgsConstructor
    public static class OperationTelemetryParams {
        String opCode;
        Integer factor;
    }

    @PostMapping(path = "/get-operation-telemetry", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResultDto> getOperationTelemetry(
        @RequestBody OperationTelemetryParams parameters) {
        return new ResponseEntity<>(
            new ApiResultDto(new ArrayList<>(), telemetryService.getOperationTelemetry(
                parameters.opCode, parameters.factor)),
            HttpStatus.OK
        );
    }
}
