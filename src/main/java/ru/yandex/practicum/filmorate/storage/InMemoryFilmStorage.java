package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(@Valid @RequestBody Film film) {
        if (isReleaseDateCorrect(film)) {
            film.setId(getNewId());
            films.put(film.getId(), film);
            log.info("Добавлен фильм {}: {}", film.getId(), film.getName());
            return film;
        }
        log.warn("Указана некорректная дата релиза {}", film.getReleaseDate());
        throw new jakarta.validation.ValidationException("Указана некорректная дата релиза");
    }

    @Override
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Не указан id при обновлении фильма");
            throw new jakarta.validation.ValidationException("Не указан id при обновлении фильма");
        }
        if (!isReleaseDateCorrect(film)) {
            log.warn("Указана некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Указана некорректная дата релиза");
        }
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {}: {} изменён", film.getId(), film.getName());
            return film;
        }
        log.warn("Фильм {} не найден", film.getId());
        throw new DataNotFoundException("Фильм " + film.getId() + " не найден");
    }

    private boolean isReleaseDateCorrect(Film film) {
        final LocalDate earliestReleaseDate = LocalDate.of(1895, 12, 27);
        return film.getReleaseDate().isAfter(earliestReleaseDate);
    }

    private long getNewId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
