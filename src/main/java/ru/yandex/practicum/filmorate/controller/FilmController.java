package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping("/films")
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping("/films")
    public Film update(@Valid @RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @GetMapping("/films/{id}")
    public Film findFilmById(@PathVariable long id) {
        return filmService.findFilmById(id);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void addLikeByUser(@PathVariable("id") long filmId, @PathVariable long userId) {
        filmService.addLikeByUser(filmId, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void deleteLikeByUser(@PathVariable("id") long filmId, @PathVariable long userId) {
        filmService.deleteLikeByUser(filmId, userId);
    }

    @GetMapping("/films/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

}