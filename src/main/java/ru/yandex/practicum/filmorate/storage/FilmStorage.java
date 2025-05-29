package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film newFilm);

    Collection<Film> findAll();

    Film findFilmById(Long filmId);

    void deleteFilmById(Long filmId);

    void addLikeByUser(Long filmId, Long userId);

    Collection<Film> getPopularFilms(Integer count);

    void removeLike(Long filmId, Long userId);

    boolean isFilmExists(Long filmId);

    Collection<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year);
}
