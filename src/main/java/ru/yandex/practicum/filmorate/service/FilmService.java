package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public final Film create(Film film) {
        validateFilm(film);
        if (film.getMpa() != null && !mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new DataNotFoundException("Рейтинг фильма не найден " + film.getMpa().getId());
        }
        if (!genreStorage.areGenresExist(
                film.getGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toList()))
        ) {
            throw new DataNotFoundException("Жанры не найдены " + film.getGenres());
        }
        Film createdFilm = filmStorage.create(film);
        log.info("Фильм {} создан", createdFilm);
        return createdFilm;
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id фильма не может быть пустым " + newFilm);
        }
        if (filmStorage.findFilmById(newFilm.getId()) != null) {
            validateFilm(newFilm);
            if (newFilm.getMpa() != null && !mpaStorage.isMpaExists(newFilm.getMpa().getId())) {
                throw new DataNotFoundException("Рейтинг фильма не найден " + newFilm.getMpa().getId());
            }
            if (!genreStorage.areGenresExist(
                    newFilm.getGenres().stream()
                            .map(Genre::getId)
                            .collect(Collectors.toList()))
            ) {
                throw new DataNotFoundException("Жанры не найдены " + newFilm.getGenres());
            }
            Film updateFilm = filmStorage.update(newFilm);
            log.info("Фильм {} обновлен", newFilm);
            return updateFilm;
        }
        throw new DataNotFoundException("Фильм не найден " + newFilm);
    }

    public Film findFilmById(Long id) {
        if (!filmStorage.isFilmExists(id)) {
            throw new DataNotFoundException("Фильм c id: " + id + " не найден");
        }
        return filmStorage.findFilmById(id);
    }

    public void addLikeByUser(Long filmId, Long userId) {
        if (!filmStorage.isFilmExists(filmId)) {
            throw new DataNotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (!userStorage.isUserExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + "не найден");
        }
        filmStorage.addLikeByUser(filmId, userId);
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void deleteLikeByUser(Long filmId, Long userId) {
        if (!filmStorage.isFilmExists(filmId)) {
            throw new DataNotFoundException("Фильм с id " + filmId + " не найден");
        }
        if (!userStorage.isUserExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
        log.debug("Пользователь {} убрал лайк у фильма {}", userId, filmId);
    }


    public Collection<Film> getPopularFilms(Integer sizeOfList) {
        return filmStorage.getPopularFilms(sizeOfList);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым : " + film);
        }

        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может быть длиннее 200 знаков: " + film);
        }

        if (film.getReleaseDate() == null || !film.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата выхода фильма не может быть раньше 1895-12-28: " + film);
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма не может быть меньше нуля: " + film);
        }
    }

    public Collection<Film> getFilmsByDirectorSorted(int directorId, String sortBy) {
        if (!sortBy.equals("year") && !sortBy.equals("likes")) {
            throw new ValidationException("Параметр sortBy должен быть 'year' или 'likes', но получен: " + sortBy);
        }

        return filmStorage.getFilmsByDirectorSorted(directorId, sortBy);
    }
}
