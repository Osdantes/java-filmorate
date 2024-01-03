package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public User addUser(User user);

    public User updateUser(User user);

    public List<User> getUsers();

    public User getUserById(Long id);

    void deleteUser(long id);

    List<User> getFriendsByUserId(long userId);

    boolean existsById(long id);
}