package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable("id") long id) {
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeByUser(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.addLikeByUser(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeByUser(@PathVariable("id") long filmId, @PathVariable("userId") long userId) {
        filmService.deleteLikeByUser(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getMostPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                          @RequestParam(required = false) Integer genreId,
                                          @RequestParam(required = false) Integer year) {
        return filmService.getMostPopularFilms(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirectorSorted(@PathVariable("directorId") int directorId,
                                                     @RequestParam(name = "sortBy", defaultValue = "likes")
                                                     String sortBy) {
        return filmService.getFilmsByDirectorSorted(directorId, sortBy);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable("filmId") long filmId) {
        filmService.deleteFilmById(filmId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query, @RequestParam String by) {
        return filmService.search(query, by);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam Integer userId, @RequestParam Integer friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }
}