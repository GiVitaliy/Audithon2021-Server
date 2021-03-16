package ru.audithon.egissostat.filestorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.FileOutputStream;
import java.io.InputStream;

@AllArgsConstructor
public class TempFileOutputStream {
    @Getter
    private TempFileInfo fileInfo;
    @Getter
    private FileOutputStream outputStream;
}
