package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Data
@Builder
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;
    @Positive
    private Integer duration;
    Set<Long> likes;

    public Set<Long> getLikes() {
        if (likes == null) {
            return new HashSet<>();
        }
        return likes;
    }
}