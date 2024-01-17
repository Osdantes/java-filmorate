package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FilmDbStorage filmDbStorage;
    private final FeedStorage feedStorage;
    private final FriendStorage friendStorage;

    public User addUser(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Wrong user data");
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        changeNameToLogin(user);
        if (isNotValid(user)) {
            throw new ValidationException("Wrong user data");
        }
        if (!existsById(user.getId())) {
            throw new DataNotFoundException("No users with id = " + user.getId() + " in DB were found.");
        }

        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public void deleteUser(long id) {
        if (isIncorrectId(id)) {
            throw new DataNotFoundException("Param must be more then 0");
        }
        userStorage.deleteUser(id);
    }

    public void addFriend(long userId, long friendId) {
        log.info(String.format("Добавление друга %d для пользователя %d.", friendId, userId));
        if (!existsById(userId) || !existsById(friendId)) {
            throw new DataNotFoundException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }
        friendStorage.addFriend(userId, friendId);
        feedStorage.addFriendRequest(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        log.info(String.format("Удаление друга %d для пользователя %d.", friendId, userId));
        if (!existsById(userId) || !existsById(friendId)) {
            throw new DataNotFoundException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }
        friendStorage.deleteFriend(userId, friendId);
        feedStorage.deleteFriendRequest(userId, friendId);
    }

    public void updateFriendRequest(long userId, long friendId) {
        if (!existsById(userId) || !existsById(friendId)) {
            throw new DataNotFoundException("No users with id = " + userId + " or " + friendId + " in DB were found.");
        }
        friendStorage.acceptFriendRequest(userId, friendId);
        feedStorage.acceptFriendRequest(userId, friendId);
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
        if (!existsById(userId) || !existsById(otherId)) {
            throw new DataNotFoundException("No users with id = " + userId + " or " + otherId + " in DB were found.");
        }

        return userStorage.getCommonFriendsByUsersIds(userId, otherId);
    }

    public List<Feed> getEventsList(long userId) {
        if (!existsById(userId)) {
            throw new DataNotFoundException("No user with id = " + userId + " in DB was found.");
        }
        return feedStorage.getFeed(userId);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }

    public List<Film> getRecommendations(long userId) {
        return filmDbStorage.getRecommendations(userId);
    }

    private boolean isNotValid(User user) {
        return user.getLogin().contains(" ")
                || user.getBirthday().isAfter(LocalDate.now());
    }

    private void changeNameToLogin(User user) {
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Changed blank user name to user login {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
