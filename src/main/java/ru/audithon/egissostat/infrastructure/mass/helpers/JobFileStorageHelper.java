package ru.audithon.egissostat.infrastructure.mass.helpers;

import com.google.common.io.Files;
import ru.audithon.egissostat.filestorage.FileStorage;
import ru.audithon.egissostat.infrastructure.mass.domain.JobKey;
import ru.audithon.common.exceptions.FileNameTooLongException;
import ru.audithon.common.helpers.InMemoryTxtLog;
import ru.audithon.common.helpers.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JobFileStorageHelper {
    private static final short MAX_FILENAME_LENGTH = 255;

    public static String writeZippedResultsToStorage(FileStorage fileStorage, JobKey jobKey, InMemoryTxtLog log,
                                                     Map<String, byte[]> files) throws IOException, FileNameTooLongException {

        LocalDateTime dateX = LocalDateTime.now();

        String resFileName = "op-res/" + dateX.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            + "/" + jobKey.getTypeId() + "/" + jobKey.getId() + ".zip";

        fileStorage.writeFile(resFileName, log.getUtf8LogBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ZipOutputStream zipOut = new ZipOutputStream(outputStream);

        ZipEntry e = new ZipEntry("журнал операции.txt");
        zipOut.putNextEntry(e);

        byte[] data = log.getUtf8LogBytes();
        zipOut.write(data, 0, data.length);
        zipOut.closeEntry();


        for (String fileName : files.keySet()) {
            validateFileNameLength(fileName);

            ZipEntry e2 = new ZipEntry(fileName);
            zipOut.putNextEntry(e2);

            byte[] data2 = files.get(fileName);
            zipOut.write(data2, 0, data2.length);
            zipOut.closeEntry();
        }

        zipOut.finish();
        zipOut.close();

        fileStorage.writeFile(resFileName, outputStream.toByteArray());

        return resFileName;
    }

    private static void validateFileNameLength(String fileName) throws FileNameTooLongException {
        if (Paths.get(fileName).getFileName().toString().getBytes().length > MAX_FILENAME_LENGTH) {
            throw new FileNameTooLongException(String.format("Имя файла %s слишком длинное для помещения в архив", fileName));
        }
    }

    /**
     * Добавляет файл в список для архива, автоматически обрезая его имя с конца до допустимой длины (в 255 байт)
     * @param files
     * @param fileName
     * @param content
     * @param path
     */
    public static void safelyPutFileLimitName(Map<String, byte[]> files, String fileName, byte[] content, String path) {
        try {
            safelyPutFile(files, fileName, content, path, true);
        } catch (FileNameTooLongException ex) {
            throw new IllegalArgumentException(String.format("Не удалось подготовить файл %s для добавления в архив из-за длинного имени", fileName));
        }
    }

    public static void safelyPutFile(Map<String, byte[]> files, String fileName, byte[] content, String path,
                                     boolean autoLimitName) throws FileNameTooLongException {

        int i = 0;

        while (true) {
            String postfix = " (" + Integer.toString(i) + ").";

            String limitedFileName;
            if (autoLimitName) {
                limitedFileName = StringUtils.limitFileNameBytes(fileName, MAX_FILENAME_LENGTH - (i > 0 ? postfix.length() : 0));
            } else {
                limitedFileName = fileName;
            }
            String fileNameProbe = i > 0
                ? Files.getNameWithoutExtension(limitedFileName) + postfix + Files.getFileExtension(limitedFileName)
                : limitedFileName;

            if (!autoLimitName) {
                validateFileNameLength(limitedFileName);
            }

            if (!files.containsKey(path + fileNameProbe)) {
                files.put(path + fileNameProbe, content);
                break;
            }

            i++;
        }
    }
}
