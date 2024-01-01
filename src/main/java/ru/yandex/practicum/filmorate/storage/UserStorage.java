package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public User addUser(User user);

    public User updateUser(User user);

    public List<User> getUsers();

    public User getUserById(Long id);

    boolean deleteUser(Integer id);

    boolean checkUserReal(int id);
}
