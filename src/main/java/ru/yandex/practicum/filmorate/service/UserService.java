package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ru.yandex.practicum.filmorate.exception.IncorrectParamException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validate.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private void validate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User create(User user, BindingResult bindingResult) {
        Validate.validate(bindingResult);
        validate(user);
        userStorage.create(user);
        log.info("Пользователь успешно создан. {}", user);
        return user;
    }

    public User update(User user, BindingResult bindingResult) {
        Validate.validate(bindingResult);
        validate(user);
        userStorage.update(user);
        log.info("Пользователь успешно обновлён. {}", user);
        return user;
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getByIdUser(long id) {
        if (userStorage.getByIdUser(id) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        return userStorage.getByIdUser(id);
    }

    public void addFriend(long idUser, long idFriend) {
        if (userStorage.getByIdUser(idUser) == null || userStorage.getByIdUser(idFriend) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        userStorage.getByIdUser(idUser).getFriends().add(idFriend);
        userStorage.getByIdUser(idFriend).getFriends().add(idUser);
    }

    public void deleteFriend(long idUser, long idFriend) {
        if (userStorage.getByIdUser(idUser) == null || userStorage.getByIdUser(idFriend) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        userStorage.getByIdUser(idUser).getFriends().remove(idFriend);
        userStorage.getByIdUser(idFriend).getFriends().remove(idUser);
    }

    public List<User> getUserFriends(long id) {
        if (userStorage.getByIdUser(id) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        List<User> friends = new ArrayList<>();
        for (long idFriend : userStorage.getByIdUser(id).getFriends()) {
            friends.add(userStorage.getByIdUser(idFriend));
        }
        return friends;
    }

    public List<User> getUserFriendsCommonWithOtherUser(long idUser, long idOtherUser) {
        if (userStorage.getByIdUser(idUser) == null || userStorage.getByIdUser(idOtherUser) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        List<Long> friendsUser = new ArrayList<>(userStorage.getByIdUser(idUser).getFriends());
        List<Long> friendsOtherUser = new ArrayList<>(userStorage.getByIdUser(idOtherUser).getFriends());
        friendsUser.retainAll(friendsOtherUser);
        return friendsUser.stream()
                .map(userStorage::getByIdUser)
                .collect(Collectors.toList());
    }
}
