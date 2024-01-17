package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikesDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikesDbStorage likesDbStorage;
    private final FeedStorage feedStorage;

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public void addLike(long userId, long filmId) {
        log.info(String.format("Добавление лайка для фильма %d от пользователя %d.", filmId, userId));
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (user != null && film != null) {
            likesDbStorage.addLike(film, user);
            feedStorage.addLike(userId, filmId);
        }
    }

    public void deleteLike(long userId, long filmId) {
        log.info(String.format("Удаление лайка для фильма %d от пользователя %d.", filmId, userId));
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (user != null && film != null) {
            likesDbStorage.deleteLike(film, user);
            feedStorage.deleteLike(userId, filmId);
        }
    }

    public List<Film> getFilmsPopularList(int count) {
        log.info(String.format("Список %d самых популярных фильмов.", count));
        return likesDbStorage.getFilmsPopularList(count);
    }

    public String deleteFilm(Integer id) {
            filmStorage.deleteFilm(id);
        return String.format("Удаление фильма по id: {}:", id);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        log.info("Список общих фильмов между двумя пользователями {} и {}", userId, friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        log.info(String.format("Список фильмов от режиссера c id = %d отсортированных по %s.", directorId, sortBy));
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> searchFilms(String query, String by) {
        log.info(String.format("Список фильмов с подстрокой query = %s по популярности.", query));
        return filmStorage.searchFilms(query, by);
    }

    public boolean existsById(long filmId) {
        return !isIncorrectId(filmId) && filmStorage.existsById(filmId);
    }

    private boolean isIncorrectId(long id) {
        return id <= 0;
    }
}
