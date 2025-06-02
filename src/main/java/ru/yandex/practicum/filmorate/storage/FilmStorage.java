package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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

    List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year);

    List<Film> getFilmsByDirectorSorted(int directorId, String sortBy);

    Set<Director> loadDirectors(Long filmId);

    void setFilmDirectors(long filmId, Set<Director> directors);

    void setFilmGenres(Long filmId, Set<Genre> genres);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    List<Like> getLikesForFilmsLikedByUser(long userId);
}
