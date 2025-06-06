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
import java.util.List;

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
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertNotNull(film.getId());
        assertEquals("Test name", film.getName());
        assertEquals(1, filmStorage.findAll().size());
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        Film updated = filmStorage.update(Film.builder()
                .id(created.getId()) // правильный ID
                .name("New test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertEquals("New test name", updated.getName());
        assertEquals(1, filmStorage.findAll().size());
    }

    @Test
    void testFindAllFilms() {
        filmStorage.create(Film.builder()
                .name("Film A")
                .description("Description A")
                .releaseDate(LocalDate.of(1980, 5, 21))
                .duration(90)
                .mpa(new Mpa(1, "G"))
                .build());

        filmStorage.create(Film.builder()
                .name("Film B")
                .description("Description B")
                .releaseDate(LocalDate.of(1985, 2, 1))
                .duration(120)
                .mpa(new Mpa(1, "G"))
                .build());

        List<Film> allFilms = (List<Film>) filmStorage.findAll();
        assertEquals(2, allFilms.size());
        assertTrue(allFilms.stream().anyMatch(f -> f.getName().equals("Film A")));
        assertTrue(allFilms.stream().anyMatch(f -> f.getName().equals("Film B")));
    }

    @Test
    void testFindFilmById() {
        Film film = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Look for me")
                .releaseDate(LocalDate.of(1980, 5, 21))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        Film found = filmStorage.findFilmById(film.getId());
        assertNotNull(found);
        assertEquals("Test name", found.getName());
    }

    @Test
    void testIsFilmExists() {
        Film film = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        assertTrue(filmStorage.isFilmExists(film.getId()));
    }
}
