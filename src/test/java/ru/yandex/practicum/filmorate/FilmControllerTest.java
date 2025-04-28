package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

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

    Film validFilm;

    @BeforeEach
    void beforeEach() {
        validFilm = Film.builder()
                .name("Test Film")
                .description("Film description")
                .releaseDate(LocalDate.of(2001, 5, 12))
                .duration(100)
                .build();
        controller = new FilmController();
    }

    @SneakyThrows
    @Test
    void testCreateValidFilm() {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void testGetAllFilms() {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void testGetAllFilmsWithNoFilmsAdded() {
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }


    @SneakyThrows
    @Test
    void testCreateFilmWithNegativeDuration() {
        validFilm.setDuration(-1);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    void testCreateFilmWithBigDescription() {
        validFilm.setDescription("a".repeat(300));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    void testCreateFilmWithBlankName() {
        validFilm.setName("");
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().is4xxClientError());
    }
}
