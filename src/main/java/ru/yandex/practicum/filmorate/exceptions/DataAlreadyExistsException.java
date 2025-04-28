package ru.yandex.practicum.filmorate.exceptions;

public class DataAlreadyExistsException extends RuntimeException {
    public DataAlreadyExistsException(String message) {
        super(message);
    }
}