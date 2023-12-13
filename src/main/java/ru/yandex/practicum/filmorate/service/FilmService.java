package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.IncorrectParamException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        filmStorage.create(film);
        log.info("Фильм успешно добавлен. {}", film);
        return film;
    }

    public Film update(Film film) {
        filmStorage.update(film);
        log.info("Фильм успешно обновлён. {}", film);
        return film;
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getByIdFilm(long id) {
        if (filmStorage.getByIdFilm(id) != null) {
            return filmStorage.getByIdFilm(id);
        } else {
            throw new IncorrectParamException("Неверный id!");
        }
    }

    public void addLike(long idFilm, long idUser) {
        if (filmStorage.getByIdFilm(idFilm) == null || userStorage.getByIdUser(idUser) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        filmStorage.getByIdFilm(idFilm).getLikes().add(idUser);
        log.info("Добавлен лайк.");
    }

    public void deleteLike(long idFilm, long idUser) {
        if (filmStorage.getByIdFilm(idFilm) == null || userStorage.getByIdUser(idUser) == null) {
            throw new IncorrectParamException("Неверный id!");
        }
        filmStorage.getByIdFilm(idFilm).getLikes().remove(idUser);
        log.info("Лайк удалён.");
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
