package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long userId, @PathVariable long id) {
        filmService.addLike(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long userId, @PathVariable long id) {
        filmService.deleteLike(userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getFilmsPopularList(@RequestParam(defaultValue = "10") int count) {
        return filmService.getFilmsPopularList(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(required = false) String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search{query}")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title") String by) {
        return filmService.searchFilms(query, by);
    }
}