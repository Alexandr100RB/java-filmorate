package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User findUserById(Long id) {
        log.debug("Вызван метод findUserById id = {}", id);
        return userStorage.findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Пользователь " + id + " не найден"));
    }

    public void addFriend(Long id, Long friendId) {
        log.debug("Пользователь {} добавил в друзья пользователя {}", id, friendId);
        User user = findUserById(id);
        User friend = findUserById(friendId);

        Set<Long> friends = user.getFriends();
        friends.add(friend.getId());
        user.setFriends(friends);

        Set<Long> friendsOfFriend = friend.getFriends();
        friendsOfFriend.add(user.getId());
        friend.setFriends(friendsOfFriend);
    }

    public void deleteFriend(Long id, Long friendId) {
        log.debug("Пользователь {} удалил из друзей пользователя {}", id, friendId);
        User user = findUserById(id);
        User friend = findUserById(friendId);

        Set<Long> friends = user.getFriends();
        friends.remove(friend.getId());
        user.setFriends(friends);

        Set<Long> friendsOfFriend = friend.getFriends();
        friendsOfFriend.remove(user.getId());
        friend.setFriends(friendsOfFriend);
    }

    public Collection<User> getFriendsByUser(Long id) {
        log.debug("Выведен список друзей пользователя {}", id);
        User user = findUserById(id);
        return user.getFriends()
                .stream()
                .map(this::findUserById)
                .toList();
    }

    public Collection<User> getCommonFriends(Long firstId, Long secondId) {
        log.debug("Выведены общие друзья пользователей {} и {}", firstId, secondId);
        User user = findUserById(firstId);
        User otherUser = findUserById(secondId);
        return user.getFriends()
                .stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::findUserById)
                .toList();
    }
}
