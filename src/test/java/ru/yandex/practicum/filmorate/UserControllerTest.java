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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserController controller;
    private User validUser;

    @BeforeEach
    void beforeEach() {
        controller = new UserController();
        validUser = User.builder()
                .email("testEmail@mail.ru")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить пользователя")
    void testCreateValidUser() {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить пользователя с некорректной датой рождения")
    void testCreateUserWithIncorrectBirthday() {
        validUser.setBirthday(LocalDate.now().plusDays(2));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить пользователя с некорректном логином")
    void testCreateUserWithIncorrectLogin() {
        validUser.setLogin("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().is4xxClientError());
    }

    @SneakyThrows
    @Test
    @DisplayName("Добавить пользователя с некорректной почтой")
    void testCreateUserWithIncorrectEmail() {
        validUser.setEmail("IncorrectEmail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().is4xxClientError());
    }
}