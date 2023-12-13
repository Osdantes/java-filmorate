package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getByIdUser(@PathVariable long id) {
        return userService.getByIdUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") long idUser, @PathVariable("friendId") long idFriend) {
        userService.addFriend(idUser, idFriend);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") long idUser, @PathVariable("friendId") long idFriend) {
        userService.deleteFriend(idUser, idFriend);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUserFriends(@PathVariable long id) {
        return userService.getUserFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getUserFriendsCommonWithOtherUser(@PathVariable("id") long idUser,
                                                        @PathVariable("otherId") long idOtherUser) {
        return userService.getUserFriendsCommonWithOtherUser(idUser, idOtherUser);
    }
}
