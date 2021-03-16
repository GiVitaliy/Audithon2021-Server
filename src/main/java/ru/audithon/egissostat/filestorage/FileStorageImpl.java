package ru.audithon.egissostat.filestorage;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import ru.audithon.common.exceptions.BusinessLogicException;
import ru.audithon.common.exceptions.FileNameTooLongException;
import ru.audithon.common.helpers.StringUtils;
import ru.audithon.common.telemetry.TelemetryServiceCore;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Repository
@EnableAsync
public class FileStorageImpl implements FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageImpl.class);

    private static final String LEGACY_FILE_SUFFIX = "@cmsfile.doc";

    private final ConcurrentHashMap<String, PreloadedFileData> preloadedFiles = new ConcurrentHashMap<>();

    @Value("${file-storage.path}")
    private String defaultStoragePath;

    private final TelemetryServiceCore telemetryServiceCore;

    public FileStorageImpl(TelemetryServiceCore telemetryServiceCore) {
        this.telemetryServiceCore = telemetryServiceCore;
    }

    public String getDefaultStoragePath() {
        return defaultStoragePath;
    }

    private Path pathFromUri(String uri) throws FileNameTooLongException {
        return pathFromUri(uri, true);
    }

    private Path pathFromUriNoValidation(String uri) {
        try {
            return pathFromUri(uri, false);
        } catch (FileNameTooLongException ex) {
            //не выполнимый кейс (ошибка не должна генерироваться без валидации)
            throw new IllegalArgumentException(String.format("Не удалось получить путь из аргумента %s из-за ошибки длинного имени", uri));
        }
    }

    private Path pathFromUri(String uri, boolean validateNameLength) throws FileNameTooLongException {
        Path path = Paths.get(defaultStoragePath + uri);

        if (validateNameLength) {
            validateFileNameLengthInPath(path);
        }

        return path;
    }

    private String getPrettyFileName(String uri) {
        // так помечаются файлы в унаследованном хранилищу (отдельный каталог файлов)
        if (uri.toLowerCase().endsWith(LEGACY_FILE_SUFFIX)) {
            return Paths.get(uri.substring(0, uri.length() - LEGACY_FILE_SUFFIX.length())).getFileName().toString();
        } else {
            return Paths.get(uri).getFileName().toString();
        }
    }

    @Override
    public void writeFile(String uri, String content)
        throws IOException, FileNameTooLongException {
        writeFile(uri, content.getBytes(Charset.forName("windows-1251")));
    }

    @Override
    public void writeFile(String uri, byte[] content)
        throws IOException, FileNameTooLongException {

        try (InputStream stream = new ByteArrayInputStream(content)) {
            writeFile(uri, stream);
        }
    }

    @Override
    public void writeFile(String uri, InputStream content)
        throws IOException, FileNameTooLongException {

        Path path = pathFromUri(uri);
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        java.nio.file.Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public OutputStream writeFileAsStream(String uri)
        throws IOException, FileNameTooLongException {

        Path path = pathFromUri(uri);
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        return new FileOutputStream(path.toFile(), false);
    }

    @Override
    public TempFileInfo writeTempFile(String fileName, InputStream fileContent)
        throws IOException {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(fileContent);

        String fileUri = getActualTempStorageUri() + fileName;
        fileUri = prettifyTempFileName(fileUri);

        try {
            writeFile(fileUri, fileContent);
        } catch (FileNameTooLongException ex) {
            throw new IOException("Ошибка имени при создании временного файла", ex);
        }

        Path absolutePath = pathFromUriNoValidation(fileUri);

        return new TempFileInfo(absolutePath.toString(), fileUri, fileName);
    }

    @Override
    public TempFileOutputStream createTempFileOutputStream(String fileName) throws IOException {
        Objects.requireNonNull(fileName);

        String fileUri = getActualTempStorageUri() + fileName;
        fileUri = prettifyTempFileName(fileUri);
        Path absolutePath = pathFromUriNoValidation(fileUri);
        TempFileInfo tempFileInfo = new TempFileInfo(absolutePath.toString(), fileUri, fileName);

        FileOutputStream stream = null;
        try {
            stream = createFileOutputStream(fileUri);

            return new TempFileOutputStream(tempFileInfo, stream);
        } catch (FileNameTooLongException ex) {
            //нереальный кейс, т.к. изначально мы обрезали имя временного файла до лимита
            throw new FileNotFoundException(String.format("Временный файл %s не создан из-за длинного имени", fileUri));
        } catch (Exception ex) {
            if (stream != null) {
                stream.close();
                deleteTempFile(tempFileInfo);
            }
            logger.info(String.format("Ошибка при создании временного файла %s", fileName), ex);
            throw ex;
        }
    }

    @Override
    public TempFileInfo zipFiles(String zipName, Set<File> files) throws IOException {
        TempFileOutputStream tempFileOutputStream = null;
        ZipOutputStream zipOut = null;
        try {
            tempFileOutputStream = createTempFileOutputStream(zipName);

            zipOut = new ZipOutputStream(tempFileOutputStream.getOutputStream());

            for (File file : files) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);

                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zipOut.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                } catch (FileNotFoundException ex) {
                    logger.info(String.format("Ошибка при добавлении файла %s в архив", file.getAbsolutePath()), ex);
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
        } catch (IOException ex) {
            logger.info("Ошибка при создании временного файла", ex);
            throw new BusinessLogicException("Не удалось создать временный файл");
        } finally {
            if (zipOut != null) {
                zipOut.close();
            }
            if (tempFileOutputStream != null) {
                tempFileOutputStream.getOutputStream().close();
            }
        }
        return tempFileOutputStream.getFileInfo();
    }

    private FileOutputStream createFileOutputStream(String uri)
        throws IOException, FileNameTooLongException {

        Path path = pathFromUri(uri);
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        return new FileOutputStream(path.toString());
    }

    @Override
    public String moveTempFile(TempFileInfo tempFileInfo, String targetFolderUri) throws IOException {
        Objects.requireNonNull(tempFileInfo);
        Objects.requireNonNull(targetFolderUri);

        Path sourcePath;

        try {
            sourcePath = pathFromUri(tempFileInfo.getUri());
        } catch (FileNameTooLongException ex) {
            // такая ситуация у нас в принципе невозможна, чтобы временный файл поступил со слишком длинным именем (они урезаютс япри создании),
            // но на крайний случаем мы точно значем, что такого файла не найдем
            throw new FileNotFoundException("Временный файл со слишком длинным именем не может быть найден: " + tempFileInfo.getUri());
        }

        if (Files.notExists(sourcePath)) {
            throw new BusinessLogicException(null,
                "Файл %s не найден для перемещения из временного расположения", tempFileInfo.getOriginalFileName());
        }

        if (!targetFolderUri.startsWith("/") && !targetFolderUri.startsWith("\\"))
            targetFolderUri = "/" + targetFolderUri;
        if (targetFolderUri.endsWith("/") || targetFolderUri.endsWith("\\"))
            targetFolderUri = targetFolderUri.substring(0, targetFolderUri.length() - 1);

        String targetSubpath = prettifyFileNameAutoLimit(targetFolderUri + "/" + tempFileInfo.getOriginalFileName());

        Path targetPath = pathFromUriNoValidation(targetSubpath);

        if (Files.notExists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }

        Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetSubpath;
    }

    public void deleteTempFile(TempFileInfo tempFileInfo) throws IOException {
        Objects.requireNonNull(tempFileInfo);

        Path sourcePath;
        try {
            sourcePath = pathFromUri(tempFileInfo.getUri());
        } catch (FileNameTooLongException ex) {
            throw new FileNotFoundException(String.format("Имя временного файла слишком длинное: %s", tempFileInfo.getUri()));
        }

        Files.deleteIfExists(sourcePath);
    }

    @Scheduled(cron = "0 0 1 * * ?") //At 01:00:00am every day
    public void cleanTempFiles() {
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::cleanTempFiles");
        try {
            try {
                List<Path> tempRootContents = Files.list(Paths.get(defaultStoragePath, ROOT_TEMP_STORAGE_URI))
                    .collect(Collectors.toList());
                String actualTempPath = getActualTempStorageUri();
                tempRootContents.forEach((path) -> {
                    if (!Files.isDirectory(path))
                        return;

                    if (path.endsWith(actualTempPath))
                        return;

                    try {
                        Files.walk(path)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    } catch (IOException ex) {
                        logger.error(String.format(
                            "Ошибка при удалении директории %s при выполнении задания на очистку временных файлов", path),
                            ex);
                    }
                });
            } catch (Exception ex) {
                logger.error("Ошибка в работе задания на очистку временных файлов", ex);
            }
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    public InputStream readFile(String uri)
        throws IOException, FileNameTooLongException {
        return readFile(uri, false);
    }

    @Override
    public InputStream readFile(String uri, boolean deleteOnClose) throws IOException, FileNameTooLongException {
        Path path = pathFromUri(uri);
        if (deleteOnClose) {
            return new DeleteOnCloseFileInputStream(path.toFile());
        }

        return new FileInputStream(path.toFile());
    }

    @SneakyThrows
    public String preloadFile(String uri) {
        try {
            return generateOneTimeDownloadLink(readFile(uri), uri);
        } catch (IOException ex) {
            throw new BusinessLogicException(null, "a18main.fileStorage.fileNotFound");
        }
    }

    @SneakyThrows
    public String generateOneTimeDownloadLink(InputStream fileContent, String uri) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(("" + Math.random() + LocalDateTime.now().toString())
            .getBytes(StandardCharsets.UTF_8));
        String hash = Base64.getEncoder().encodeToString(hashBytes);
        preloadedFiles.put(hash, new PreloadedFileData(fileContent, uri, getPrettyFileName(uri), LocalDateTime.now()));
        return hash;
    }

    public PreloadedFileData readPreloadedFile(String preloadHashCode) {
        return preloadedFiles.remove(preloadHashCode);
    }

    public String getPersonStorageUri(Integer personId) {
        String part1 = Integer.toString(personId % 1000);
        String part2 = Integer.toString((personId / 1000) % 1000);
        String part3 = Integer.toString(personId / 1000000);
        part1 = "000".substring(0, 3 - part1.length()) + part1;
        part2 = "000".substring(0, 3 - part2.length()) + part2;
        return "uploads/person/" + part3 + "/" + part2 + "/" + part1 + "/";
    }

    @Override
    public String getPersonRequestStorageUri(Integer personId, Integer requestId) {
        return getPersonStorageUri(personId) + "request/" + Integer.toString(requestId) + "/";
    }

    public String getInstitutionStorageUri(Integer institutionId) {
        return "uploads/institution/" + institutionId + "/";
    }

    private static final String ROOT_TEMP_STORAGE_URI = "uploads/temp/";

    private String getActualTempStorageUri() {
        return ROOT_TEMP_STORAGE_URI + LocalDate.now().toEpochDay() + "/";
    }

    @Scheduled(fixedDelay = 60000)
    public void evictPreloadedFilesCache() {
        TelemetryServiceCore.TelemetryOperationToken optoken = telemetryServiceCore.enterOperation("@Scheduled::evictPreloadedFilesCache");
        try {
            preloadedFiles.forEach((hash, item) -> {
                if (Duration.between(item.getCreatedTime(), LocalDateTime.now()).toMillis() > 55000) {
                    try {
                        preloadedFiles.get(hash).getContent().close();
                    } catch (Exception e) {
                    }
                    preloadedFiles.remove(hash);
                }
            });
        } finally {
            telemetryServiceCore.exitOperation(optoken);
        }
    }

    @Override
    public String prettifyFileName(String fileUri) throws FileNameTooLongException {
        return prettifyFileName(fileUri, false);
    }

    private String prettifyFileNameAutoLimit(String fileUri) {
        try {
            return prettifyFileName(fileUri, true);
        } catch (FileNameTooLongException ex) {
            //невыполнимый кейс, т.к. ошибка может генерироваться только без автообрезке имени файла
            throw new IllegalArgumentException(String.format("Не удалось обработать путь к файлу %s из-за ошибки длинного имени", fileUri));
        }
    }

    private String prettifyFileName(String fileUri, boolean autoLimitName) throws FileNameTooLongException {
        Path path;

        if (autoLimitName) {
            fileUri = limitUriToMaxLength(fileUri);
            path = pathFromUriNoValidation(fileUri);
        } else {
            path = pathFromUri(fileUri);
        }

        if (Files.notExists(path)) {
            return fileUri;
        } else {
            String originFileName = path.getFileName().toString();
            // рано или поздно мы найдем незанятый номер - не может же быть бесконечное число файлов в директории.
            //даже если их много - не беда. Просто будет подтормаживать с вероятностью 0.00000001%, т.к. это исключительный случай
            for (int i = 1; true; i++) {
                String postfix = " (" + i + ")";
                String fileName = autoLimitName ? limitToMaxLength(originFileName, postfix.length()) : originFileName;

                int dotPos = fileName.lastIndexOf('.');
                if (dotPos >= 0) {
                    fileName = fileName.substring(0, dotPos) + postfix + fileName.substring(dotPos);
                } else {
                    fileName = fileName + postfix;
                }

                Path newPath = Paths.get(path.getParent().toString(), fileName);

                if (Files.notExists(newPath)) {
                    return Paths.get(Paths.get(fileUri).getParent().toString(), fileName).toString();
                }
            }

        }
    }

    public String prettifyTempFileName(String fileUri) {
        Path path = Paths.get(defaultStoragePath + fileUri);
        String fileName = path.getFileName().toString();

        for (int i = 0; true; i++) {
            String tempFileName = limitToMaxLength(String.valueOf(i) + "_" + fileName);
            Path newPath = Paths.get(path.getParent().toString(), tempFileName);

            if (Files.notExists(newPath)) {
                return Paths.get(Paths.get(fileUri).getParent().toString(), tempFileName).toString();
            }
        }
    }

    private void validateFileNameLengthInPath(Path filePath) throws FileNameTooLongException {
        Objects.requireNonNull(filePath, "Путь к файлу не задан");

        validateFileNameLength(filePath.getFileName().toString());
    }

    private static final short MAX_FILENAME_LENGTH = 255;

    private void validateFileNameLength(String fileName) throws FileNameTooLongException {
        Objects.requireNonNull(fileName, "Имя файла не задано");

        if (fileName.getBytes().length > MAX_FILENAME_LENGTH) {
            throw new FileNameTooLongException(fileName);
        }
    }

    public static String limitUriToMaxLength(String fileUri) {
        Path path = Paths.get(fileUri);
        String limitedFileName = limitToMaxLength(path.getFileName().toString());
        return fileUri.substring(0, fileUri.length() - path.getFileName().toString().length()) + limitedFileName;
    }

    public static String limitToMaxLength(String fileName, int reservedSublength) {
        return StringUtils.limitFileNameBytes(fileName, MAX_FILENAME_LENGTH - reservedSublength);
    }

    public static String limitToMaxLength(String fileName) {
        return limitToMaxLength(fileName, 0);
    }

}
