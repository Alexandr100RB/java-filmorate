package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(@RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(getNewId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}: {}", user.getId(), user.getName());
        return user;
    }

    @Override
    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            log.warn("Не указан id при обновлении пользователя");
            throw new ValidationException("Не указан id при обновлении пользователя");
        }
        if (users.containsKey(user.getId())) {
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            log.info("Пользователь {}: {} изменён", user.getId(), user.getName());
            return user;
        }
        log.warn("Пользователь {} не найден", user.getId());
        throw new DataNotFoundException("Пользователь " + user.getId() + " не найден");
    }

    private long getNewId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
