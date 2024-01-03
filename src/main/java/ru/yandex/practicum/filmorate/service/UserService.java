package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendsDbStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendsDbStorage friendsDbStorage;

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void addFriend(long userId, long friendId) {
        log.info(String.format("Добавление друга %d для пользователя %d.", friendId, userId));
        User requestUser = userStorage.getUserById(friendId);
        User acceptUser = userStorage.getUserById(userId);
        if (requestUser != null && acceptUser != null) {
            friendsDbStorage.addFriendRequest(requestUser, acceptUser);
            friendsDbStorage.acceptFriendRequest(requestUser, acceptUser);
        }
    }

    public void deleteFriend(long userId, long friendId) {
        log.info(String.format("Удаление друга %d для пользователя %d.", friendId, userId));
        User requestUser = userStorage.getUserById(friendId);
        User acceptUser = userStorage.getUserById(userId);
        if (requestUser != null && acceptUser != null) {
            friendsDbStorage.deleteFriend(requestUser, acceptUser);
        }
    }


    public List<User> getFriendsList(long userId) {
        if (!existsById(userId)) {
            throw new DataNotFoundException("No user with id = " + userId + " in DB was found.");
        }

        return userStorage.getFriendsByUserId(userId);
    }

    public boolean existsById(long userId) {
        return !isIncorrectId(userId) && userStorage.existsById(userId);
    }

    public List<User> getCommonFriendsList(long userId, long otherId) {
        return friendsDbStorage.getCommonFriendsList(userId, otherId);
    }


    public String deleteUser(long id) {
        if (userStorage.getUserById(id) != null) {
            userStorage.deleteUser(id);
            return String.format("Пользователь с id %s удален", id);
        } else {
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }
}