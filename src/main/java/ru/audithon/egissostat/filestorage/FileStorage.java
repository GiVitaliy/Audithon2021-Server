package ru.audithon.egissostat.filestorage;

import ru.audithon.common.exceptions.FileNameTooLongException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.zip.ZipEntry;

public interface FileStorage {

    void writeFile(String uri, String content)
        throws IOException, FileNameTooLongException;
    void writeFile(String uri, byte[] content)
        throws IOException, FileNameTooLongException;
    void writeFile(String uri, InputStream content)
        throws IOException, FileNameTooLongException;
    OutputStream writeFileAsStream(String uri)
        throws IOException, FileNameTooLongException;
    InputStream readFile(String uri) throws IOException, FileNameTooLongException;
    InputStream readFile(String uri, boolean deleteOnClose) throws IOException, FileNameTooLongException;
    String preloadFile(String uri);
    String generateOneTimeDownloadLink(InputStream fileContent, String uri);
    PreloadedFileData readPreloadedFile(String preloadHashCode);
    String getPersonStorageUri(Integer personId);
    String getPersonRequestStorageUri(Integer personId, Integer requestId);
    String getInstitutionStorageUri(Integer institutionId);
    String prettifyFileName(String fileUri) throws FileNameTooLongException;
    TempFileInfo writeTempFile(String fileName, InputStream fileContent) throws IOException;
    void cleanTempFiles();
    TempFileOutputStream createTempFileOutputStream(String fileName) throws IOException;
    String moveTempFile(TempFileInfo tempFileInfo, String targetFolderUri) throws  IOException;
    void deleteTempFile(TempFileInfo tempFileInfo) throws  IOException;
    TempFileInfo zipFiles(String zipName, Set<File> files) throws IOException;
    String getDefaultStoragePath();
    void evictPreloadedFilesCache();
}

