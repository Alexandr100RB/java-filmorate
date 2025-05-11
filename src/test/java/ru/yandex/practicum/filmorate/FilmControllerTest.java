package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {
    private FilmController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Film validFilm;

    @BeforeEach
    void beforeEach() {
        validFilm = Film.builder()
                .name("Test Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2001, 5, 12))
                .duration(100)
                .build();
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        FilmService filmService = new FilmService(filmStorage, userService);
        controller = new FilmController(filmService);
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить фильм")
    void testCreateValidFilm() {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получить список фильмов")
    void testGetAllFilms() {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @DisplayName("Получить пустой список фильмов")
    void testGetAllFilmsWithNoFilmsAdded() {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }


    @SneakyThrows
    @Test
    @DisplayName("Добавить фильм с отрицательной длительностью")
    void testCreateFilmWithNegativeDuration() {
        validFilm.setDuration(-1);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить фильм с длинным описанием")
    void testCreateFilmWithBigDescription() {
        validFilm.setDescription("a".repeat(300));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить фильм с пустым названием")
    void testCreateFilmWithBlankName() {
        validFilm.setName("");
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }
}