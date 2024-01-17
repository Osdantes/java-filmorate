package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    List<Film> getCommonFilms(long userId, long friendId);

    Film getFilmById(Long id);

    List<Film> getFilmsByDirector(int directorId, String sortBy);

    List<Film> getRecommendations(long userId);

    List<Film> searchFilms(String query, String by);

    void deleteFilm(Integer id);

    boolean existsById(long id);
}
