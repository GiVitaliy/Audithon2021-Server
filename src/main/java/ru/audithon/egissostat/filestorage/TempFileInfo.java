package ru.audithon.egissostat.filestorage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TempFileInfo {
    /**
     * Абсолютный путь к временному файлу
     */
    private String absolutePath;
    /**
     * Путь к файлу относительно корневой папки хранилища
     */
    private String uri;
    /**
     * Оригинальное имя файла (временный файл получает уникальное имя)
     */
    private String originalFileName;
}
