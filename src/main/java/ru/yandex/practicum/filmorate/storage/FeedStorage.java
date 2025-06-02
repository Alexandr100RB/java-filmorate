package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;
import java.util.Optional;

public interface FeedStorage {

    void addFeed(long userId, long entityId, long eventTypeId, long operationTypeId, long timestamp);

    List<Feed> findFeedsForUser(Long userId);

    Optional<Long> findOEventTypeIdByName(EventTypes eventType);

    Optional<Long> findOperationTypeIdByName(OperationTypes operationType);
}
