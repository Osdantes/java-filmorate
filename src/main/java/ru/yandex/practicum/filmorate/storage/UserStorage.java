package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUserById(Long id);

    void deleteUser(long id);

    List<User> getFriendsByUserId(long userId);

    boolean existsById(long id);

    User getUserById(Long id);
}
