package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film findFilmById(Long id) {
        return filmStorage.findAll().stream()
                .filter(film -> film.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Фильм " + id + " не найден"));
    }

    public void addLikeByUser(Long filmId, Long userId) {
        log.debug("Пользователь {} поставил лайк фильму {}", userId, filmId);
        Film film = findFilmById(filmId);
        User user = userService.findUserById(userId);

        Set<Long> likes = film.getLikes();
        likes.add(user.getId());
        film.setLikes(likes);
    }

    public void deleteLikeByUser(Long filmId, Long userId) {
        log.debug("Пользователь {} убрал лайк у фильма {}", userId, filmId);
        Film film = findFilmById(filmId);
        User user = userService.findUserById(userId);

        Set<Long> likes = film.getLikes();
        likes.remove(user.getId());
        film.setLikes(likes);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Выведены самые популярные фильмы");
        return filmStorage.findAll()
                .stream()
                .sorted(Film.compareByLikes())
                .limit(count)
                .toList();
    }
}
