package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
@AllArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;
    public Director create(Director director) {
        Director createdDirector = directorStorage.create(director);
        log.info(String.format("Добавлен режиссёр %s", director));
        return createdDirector;
    }

    public Director update(Director newDirector) {
        if (newDirector.getId() == null) {
            throw new ValidationException("Id режиссёра не может быть пустым " + newDirector);
        }
        if (!directorStorage.isDirectorExists(newDirector.getId())) {
            throw new DataNotFoundException("Режиссёр не найден " + newDirector);
        }
        Director updetedDirector = directorStorage.update(newDirector);
        log.info(String.format("Обновлен режиссёр %s", newDirector));
        return updetedDirector;
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(int directorId) {
        if (!directorStorage.isDirectorExists(directorId)) {
            throw new DataNotFoundException("Режиссёр с id " + directorId + "не найден");
        }
        return directorStorage.getDirectorById(directorId);
    }

    public void deleteDirectorById(int directorId) {
        if (!directorStorage.isDirectorExists(directorId)) {
            throw new DataNotFoundException("Режиссёр с id " + directorId + "не найден");
        }
        directorStorage.deleteDirectorById(directorId);
        log.debug("Режиссёр {} удалён", directorId);
    }
}