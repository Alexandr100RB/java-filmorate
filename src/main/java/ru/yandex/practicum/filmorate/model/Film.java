package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Getter
@Setter

@Data
@Builder
public class Film {
    Long id;
    @NotBlank
    String name;
    @Size(max = 200)
    String description;
    LocalDate releaseDate;
    @Positive
    Integer duration;
}