package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Director {
    private Integer id;
    @Pattern(regexp = "^(?!\\s).*")
    private String name;
}
