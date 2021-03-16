package ru.audithon.common.exceptions;

public class FileNameTooLongException extends Exception { //наследуемся не от IOException, чтобы иметь свое обрабатываемое исключение
    public FileNameTooLongException(String fileName) {
        super(String.format("Имя файла слишком длинное: %s", fileName));
    }
}
