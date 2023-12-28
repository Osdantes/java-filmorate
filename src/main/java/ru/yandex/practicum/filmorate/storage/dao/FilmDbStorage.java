package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;


    private boolean isValid(Film film) {
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(Constants.MIN_RELEASE_DATE)) {
            throw new ValidationException("Система поддерживает загрузку фильмов с датой выхода после "
                    + Constants.MIN_RELEASE_DATE.format(Constants.FORMATTER) + ".");
        }
        if (film.getDescription() != null && film.getDescription().length() > Constants.DESCRIPTION_LENGTH) {
            throw new ValidationException("Длина описания должна быть меньше "
                    + Constants.MIN_RELEASE_DATE.format(Constants.FORMATTER) + ".");
        }
        return true;
    }


    public Film addFilm(Film film) {
        if (!isValid(film)) {
            throw new ValidationException("Фильм не прошел валидацию.");
        }

        String sqlInsertFilm = "INSERT INTO films(name, description, duration, " +
                (film.getMpa() != null ? "mpa_code, " : "") + "release_date) VALUES(?, ?, ?, " +
                (film.getMpa() != null ? "?, " : "") + "?)";

        KeyHolder keyHolderFilmId = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsertFilm, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(4, film.getMpa().getId());
                ps.setDate(5, Date.valueOf(film.getReleaseDate()));
            } else {
                ps.setDate(4, Date.valueOf(film.getReleaseDate()));
            }
            return ps;
        }, keyHolderFilmId);

        if (keyHolderFilmId.getKey() == null) {
            throw new RuntimeException("Ошибка при добавлении фильма.");
        }

        film.setId(keyHolderFilmId.getKey().longValue());
        log.info(String.format("Добавлен новый фильм %d.", film.getId()));

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre ->
                    jdbcTemplate.update("INSERT INTO genre_link(film_id, genre_code) VALUES (?, ?)",
                            film.getId(), genre.getId()));
        }

        return getFilmById(film.getId());
    }


    public Film updateFilm(Film film) {
        if (film.getId() == 0 || !isValid(film) || getFilmById(film.getId()) == null) {
            throw new DataNotFoundException(String.format("Не существует фильма с заданным id %d.", film.getId()));
        }

        // Определение SQL запроса
        String sqlUpdateFilm = "UPDATE films SET name = ?, description = ?, duration = ?, " +
                (film.getMpa() != null ? "mpa_code = ?, " : "") + "release_date = ? WHERE id = ?";

        // Подготовка параметров
        List<Object> parameters = new ArrayList<>(Arrays.asList(
                film.getName(),
                film.getDescription(),
                film.getDuration()
        ));
        if (film.getMpa() != null) {
            parameters.add(film.getMpa().getId());
        }
        parameters.add(film.getReleaseDate());
        parameters.add(film.getId());

        // Обновление информации о фильме
        jdbcTemplate.update(sqlUpdateFilm, parameters.toArray());

        updateFilmGenres(film); // Обновление жанров фильма

        log.info(String.format("Изменена информация о фильме %d.", film.getId()));
        return getFilmById(film.getId());
    }

    private void updateFilmGenres(Film film) {
        List<Integer> genreListIdFromDb = genreDbStorage.findGenreByFilmId(film.getId())
                .stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());
        List<Integer> genreListIdFromFilm = film.getGenres() == null ? Collections.emptyList() :
                film.getGenres()
                        .stream()
                        .map(Genre::getId)
                        .distinct()
                        .collect(Collectors.toList());

        // Добавление новых жанров
        for (Integer genreId : genreListIdFromFilm) {
            if (!genreListIdFromDb.contains(genreId)) {
                jdbcTemplate.update("INSERT INTO genre_link(film_id, genre_code) VALUES (?, ?)",
                        film.getId(), genreId);
            }
        }

        // Удаление старых жанров
        for (Integer genreId : genreListIdFromDb) {
            if (!genreListIdFromFilm.contains(genreId)) {
                jdbcTemplate.update("DELETE FROM genre_link WHERE film_id = ? AND genre_code = ?",
                        film.getId(), genreId);
            }
        }
    }


    public List<Film> getFilms() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }


    public Film getFilmById(Long id) {
        String sqlQuery = "SELECT * FROM films WHERE id = ?";
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlQuery, id);

        if (!filmRows.next()) {
            throw new DataNotFoundException(String.format("Не найден фильм с заданным id %d.", id));
        }

        // Создание построителя фильма
        Film.FilmBuilder filmBuilder = Film.builder()
                .id(id)
                .name(filmRows.getString("name"))
                .description(filmRows.getString("description"))
                .duration(filmRows.getInt("duration"))
                .releaseDate(Objects.requireNonNull(filmRows.getDate("release_date")).toLocalDate())
                .genres(genreDbStorage.findGenreByFilmId(id)); // Получение и добавление жанров

        // Добавление MPA рейтинга, если он существует
        int mpaCode = filmRows.getInt("mpa_code");
        if (mpaCode > 0) {
            Mpa mpa = mpaDbStorage.findPmaByCode(mpaCode);
            filmBuilder.mpa(mpa);
        }

        return filmBuilder.build();
    }


    public List<Film> getFilmsPopularList(int count) {
        String sql = "select f.* from films f left join " +
                "(select ll.film_id, count(ll.user_id) cnt from likes_link ll group by ll.film_id) l " +
                "on f.id = l.film_id " +
                "order by l.cnt desc " +
                "limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        final String sql = "select f.* " +
                "from films f " +
                "join likes_link l1 on f.id = l1.film_id " +
                "join likes_link l2 on f.id = l2.film_id " +
                "where l1.user_id = ? " +
                "and l2.user_id = ? " +
                "and l1.user_id <> l2.user_id";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId, friendId);
        System.out.println(films);
        return films;
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        List<Genre> genre = genreDbStorage.findGenreByFilmId(id);
        log.info("Фильм с id = {}", id);
        int mpa_id = rs.getInt("mpa_code");
        if (mpa_id > 0) {
            Mpa mpa = mpaDbStorage.findPmaByCode(rs.getInt("mpa_code"));
            return getDefaultFilm(rs)
                    .mpa(mpa)
                    .genres(genre)
                    .build();
        } else {
            return getDefaultFilm(rs)
                    .genres(genre)
                    .build();
        }
    }

    public Film.FilmBuilder getDefaultFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .duration(rs.getInt("duration"))
                .releaseDate(rs.getDate("release_date").toLocalDate());
    }
}