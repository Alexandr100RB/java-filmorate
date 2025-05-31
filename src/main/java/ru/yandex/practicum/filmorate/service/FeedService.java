package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Autowired
    public FeedService(FeedStorage feedStorage, UserStorage userStorage) {
        this.feedStorage = feedStorage;
        this.userStorage = userStorage;
    }

    public List<Feed> getFeedByUserId(long userId) {
        if (!userStorage.isUserExists(userId)) {
            throw new DataNotFoundException(String.format("Пользователь с Id %s не найден.", userId));
        }
        return feedStorage.findFeedsForUser(userId);
    }

    public void addFeed(long userId, long entityId, EventTypes eventType, OperationTypes operationType) {
        Optional<Long> eventTypeId = feedStorage.findOEventTypeIdByName(eventType);
        if (eventTypeId.isEmpty()) {
            throw new DataNotFoundException(String.format("EventType с названием %s не найден.", eventType));
        }
        Optional<Long> operationTypeId = feedStorage.findOperationTypeIdByName(operationType);
        if (operationTypeId.isEmpty()) {
            throw new DataNotFoundException(String.format("OperationTypeId с названием %s не найден.", operationType));
        }
        feedStorage.addFeed(userId, entityId, eventTypeId.get(), operationTypeId.get(), Instant.now().toEpochMilli());
    }
}