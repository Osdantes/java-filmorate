package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
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
        }
    }

    public void deleteLike(long userId, long filmId) {
        log.info(String.format("Удаление лайка для фильма %d от пользователя %d.", filmId, userId));
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (user != null && film != null) {
            likesDbStorage.deleteLike(film, user);
        }
    }

    public List<Film> getFilmsPopularList(Integer count, Integer genreId, Integer year) {
        log.info(String.format("Список %d самых популярных фильмов.", count));
        return likesDbStorage.getFilmsPopularList(count, genreId, year);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        log.info(String.format("Список фильмов от режиссера c id = %d отсортированных по %s.", directorId, sortBy));
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }
}
