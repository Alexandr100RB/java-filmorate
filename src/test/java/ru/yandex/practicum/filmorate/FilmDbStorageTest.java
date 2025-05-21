package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, GenreDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
public class FilmDbStorageTest {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testCreateFilm() {
        Film film = filmStorage.create(Film.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertNotNull(film.getName());
        assertEquals("Test name", film.getName());
        assertEquals(1, filmStorage.findAll().size());
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.create(Film.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        film = filmStorage.update(Film.builder()
                .id(1L)
                .name("New test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertEquals("New test name", film.getName());
        assertEquals(1, filmStorage.findAll().size());
    }

    @Test
    void testFindAllFilms() {
        Film film = filmStorage.create(Film.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        Film newFilm = filmStorage.create(Film.builder()
                .id(2L)
                .name("New test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1985-02-01"))
                .duration(170)
                .mpa(new Mpa(1, "G"))
                .build());

        assertNotNull(filmStorage.findAll());
        assertEquals(2, filmStorage.findAll().size());
    }

    @Test
    void testFindFilmById() {
        Film film = filmStorage.create(Film.builder()
                .id(5L)
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertNotNull(film);
        assertEquals(5L, film.getId());
    }

    @Test
    void testIsFilmExists() {
        Film film = filmStorage.create(Film.builder()
                .id(1L)
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertTrue(filmStorage.isFilmExists(film.getId()));
    }
}
