package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class User {
    private Long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
    Set<Long> friends;
    public Set<Long> getFriends() {
        if (friends == null) {
            return new HashSet<>();
        }
        return friends;
    }
}