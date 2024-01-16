package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Requested creation of friend {} to user {}", friendId, id);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Requested deletion of friend {} from user {}", friendId, id);
        userService.deleteFriend(id, friendId);
    }

    @PostMapping("/{id}/friends/{friendId}")
    public void acceptFriendRequest(@PathVariable long id, @PathVariable long friendId) {
        log.info("Accept request for friendship with user {} from user {}", friendId, id);
        userService.updateFriendRequest(id, friendId);
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getEventsList(@PathVariable long id) {
        log.info("Get events for user {}", id);
        return userService.getEventsList(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsList(@PathVariable long id) {
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriendsList(@PathVariable long id, @PathVariable long otherId) {
        log.info("Requested common friend list between user {} and other user {}", id, otherId);
        return userService.getCommonFriendsList(id, otherId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
    }
}