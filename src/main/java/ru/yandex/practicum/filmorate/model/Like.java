package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;


@Data
@Builder
@Setter(AccessLevel.NONE)
public class Like {
    private long userId;
    @NotNull
    private long filmId;
}
