package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.List;

public interface DirectorStorage {
    Director getDirectorById(Integer directorId);

    Collection<Director> findAll();

    boolean isDirectorExists(Integer directorId);

    boolean areDirectorsExist(List<Integer> directorIds);

    void deleteDirectorById(int directorId);

    Director create(Director director);

    Director update(Director director);
}
