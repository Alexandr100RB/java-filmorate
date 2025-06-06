package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Feed {
    @NotNull
    private Long eventId;
    @NotNull
    private Long userId;
    @NotNull
    private Long entityId;
    @NotNull
    private EventTypes eventType;
    @NotNull
    private OperationTypes operation;
    @NotNull
    private Long timestamp;
}