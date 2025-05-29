package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validateUser(user);
        if (userStorage.isUserExistsWithEmail(user)) {
            throw new ValidationException("Поле e-mael должно быть заполнено");
        }

        User createdUser = userStorage.create(user);
        log.info(String.format("Добавлен пользователь %s", user));
        return createdUser;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id пользователя не может быть пустым " + newUser);
        }
        if (!userStorage.isUserExists(newUser.getId())) {
            throw new DataNotFoundException("Пользователь не найден " + newUser);
        }
        validateUser(newUser);
        if (userStorage.isUserExistsWithEmail(newUser)) {
            throw new ValidationException("Пользователь с таким e-mail уже существует " + newUser.getEmail());
        }
        User updetedUser = userStorage.update(newUser);
        log.info(String.format("Обновлен пользователь %s", newUser));
        return updetedUser;
    }

    public User findUserById(Long id) {
        if (!userStorage.isUserExists(id)) {
            throw new DataNotFoundException(String.format("Пользователь с Id %s не найден.", id));
        }
        log.debug("Вызван метод findUserById id = {}", id);
        return userStorage.getUserById(id);
    }

    public void addFriend(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить в друзья сам себя " + id);
        }
        if (!userStorage.isUserExists(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + "не найден");
        }
        if (!userStorage.isUserExists(friendId)) {
            throw new DataNotFoundException("Друг с id " + id + "не найден");
        }
        userStorage.addFriend(id, friendId);
        log.debug("Пользователь {} добавил в друзья пользователя {}", id, friendId);
    }

    public void deleteFriend(Long id, Long friendId) {
        if (!userStorage.isUserExists(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + "не найден");
        }
        if (!userStorage.isUserExists(friendId)) {
            throw new DataNotFoundException("Друг с id " + id + "мне найден");
        }
        userStorage.deleteFriend(id, friendId);
        log.debug("Пользователь {} удалил из друзей пользователя {}", id, friendId);
    }

    public Collection<User> getFriendsByUser(Long id) {
        if (!userStorage.isUserExists(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + "мне найден");
        }
        log.debug("Выведен список друзей пользователя {}", id);
        return userStorage.getFriendsByUser(id);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        if (!userStorage.isUserExists(userId)) {
            throw new DataNotFoundException("Пользователь с id " + userId + " не найден");
        }
        if (!userStorage.isUserExists(otherId)) {
            throw new DataNotFoundException("Пользователь с id " + otherId + " не найден");
        }
        log.debug("Выведены общие друзья пользователей {} и {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("email пользователя не может быть пустым и должен содержать @: " + user);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин Пользователя не может быть пустым: " + user);
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("День рождения Пользователя не может быть пустым или в будущем: " + user);
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void deleteUserById(Long id) {
        if (!userStorage.isUserExists(id)) {
            throw new DataNotFoundException("Пользователь с id " + id + "не найден");
        }
        userStorage.deleteUserById(id);
        log.debug("Пользователь {} удалён", id);
    }
}
