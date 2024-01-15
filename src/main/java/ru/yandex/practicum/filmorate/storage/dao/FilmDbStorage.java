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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Repository
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;

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

        String sqlInsertFilm = "INSERT INTO films (name, description, duration, mpa_code, release_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolderFilmId = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlInsertFilm, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setInt(3, film.getDuration());

            if (film.getMpa() != null) {
                ps.setInt(4, film.getMpa().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setDate(5, Date.valueOf(film.getReleaseDate()));

            return ps;
        }, keyHolderFilmId);

        if (keyHolderFilmId.getKey() != null) {
            film.setId(keyHolderFilmId.getKey().longValue());

            updateFilmGenres(film);
            updateFilmDirectors(film);

            log.info(String.format("Добавлен новый фильм %d.", film.getId()));
        }
        return getFilmById(film.getId());
    }

    public Film updateFilm(Film film) {
        if (film.getId() == 0 || !isValid(film) || getFilmById(film.getId()) == null) {
            throw new DataNotFoundException(String.format("Не существует фильм с заданным id %d.", film.getId()));
        }

        String sqlUpdateFilm = "UPDATE films SET name = ?, description = ?, duration = ?," +
                " mpa_code = ?, release_date = ? WHERE id = ?";

        jdbcTemplate.update(sqlUpdateFilm,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                Date.valueOf(film.getReleaseDate()),
                film.getId());

        updateFilmGenres(film);
        updateFilmDirectors(film);

        log.info(String.format("Изменена информация о фильме %d.", film.getId()));
        return getFilmById(film.getId());
    }

    public List<Film> getFilms() {
        String sql = "select * from films";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    public void deleteFilms() {
        jdbcTemplate.execute("truncate table films restart identity");
    }

    public Film getFilmById(Long id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);

        if (!filmRows.next()) {
            throw new DataNotFoundException(String.format("Не найден фильм с заданным id %d.", id));
        }

        List<Genre> genres = genreDbStorage.findGenreByFilmId(id);
        Mpa mpa = null;
        if (filmRows.getInt("mpa_code") > 0) {
            mpa = mpaDbStorage.findPmaByCode(filmRows.getInt("mpa_code"));
        }
        List<Director> directors = directorDbStorage.findDirectorsByFilmId(id);

        return Film.builder()
                .id(id)
                .name(filmRows.getString("name"))
                .description(filmRows.getString("description"))
                .duration(filmRows.getInt("duration"))
                .releaseDate(filmRows.getDate("release_date").toLocalDate())
                .mpa(mpa)
                .directors(directors)
                .genres(genres)
                .build();
    }

    public void deleteFilm(Integer id) {
        jdbcTemplate.update("delete from films where id=?", id);
        log.info("Фильм удален id={}", id);
    }

    public List<Film> getFilmsPopularList(int count) {
        String sql = "select f.* from films f left join " +
                "(select ll.film_id, count(ll.user_id) cnt from likes_link ll group by ll.film_id) l " +
                "on f.id = l.film_id " +
                "order by l.cnt desc " +
                "limit ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), count);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (directorDbStorage.getDirectorById(directorId) == null) {
            throw new DataNotFoundException(String.format("Не найден режиссер с заданным id %d.", directorId));
        }
        String sql = "SELECT f.*, " +
                "(SELECT COUNT(*) FROM likes_link WHERE film_id = f.id) as likes_count " +
                "FROM films f " +
                "JOIN film_directors fd ON f.id = fd.film_id " +
                "WHERE fd.director_id = ? ";

        if ("likes".equals(sortBy)) {
            sql += "ORDER BY likes_count DESC";
        } else if ("year".equals(sortBy)) {
            sql += "ORDER BY f.release_date";
        } else {
            // дефолтный порядок
            sql += "ORDER BY f.name";
        }

        return jdbcTemplate.query(sql, new Object[]{directorId}, (rs, rowNum) -> makeFilm(rs));
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

        for (Integer genreId : genreListIdFromFilm) {
            if (!genreListIdFromDb.contains(genreId)) {
                jdbcTemplate.update("INSERT INTO genre_link(film_id, genre_code) VALUES (?, ?)",
                        film.getId(), genreId);
            }
        }

        for (Integer genreId : genreListIdFromDb) {
            if (!genreListIdFromFilm.contains(genreId)) {
                jdbcTemplate.update("DELETE FROM genre_link WHERE film_id = ? AND genre_code = ?",
                        film.getId(), genreId);
            }
        }
    }

    public boolean existsById(long id) {
        Integer count = jdbcTemplate.queryForObject("select count(1) from films where id=?", Integer.class, id);
        return count == 1;
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        final String sql = "select f.* " +
                "from films f " +
                "join likes_link l1 on f.id = l1.film_id " +
                "join likes_link l2 on f.id = l2.film_id " +
                "where l1.user_id = ? " +
                "and l2.user_id = ? " +
                "and l1.user_id <> l2.user_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), userId, friendId);
    }

    private void updateFilmDirectors(Film film) {
        List<Integer> directorListIdFromDb = directorDbStorage.findDirectorsByFilmId(film.getId())
                .stream()
                .map(Director::getId)
                .distinct()
                .collect(Collectors.toList());
        List<Integer> directorListIdFromFilm = film.getDirectors() == null ? Collections.emptyList() :
                film.getDirectors()
                        .stream()
                        .map(Director::getId)
                        .distinct()
                        .collect(Collectors.toList());

        for (Integer directorId : directorListIdFromFilm) {
            if (!directorListIdFromDb.contains(directorId)) {
                jdbcTemplate.update("INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)",
                        film.getId(), directorId);
            }
        }

        for (Integer directorId : directorListIdFromDb) {
            if (!directorListIdFromFilm.contains(directorId)) {
                jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ? AND director_id = ?",
                        film.getId(), directorId);
            }
        }
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        List<Genre> genres = genreDbStorage.findGenreByFilmId(id);

        List<Director> directors = jdbcTemplate.query(
                "SELECT d.* FROM film_directors fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id = ?",
                new Object[]{id},
                (rsDirector, rowNum) -> new Director(rsDirector.getInt("id"), rsDirector.getString("name"))
        );

        Mpa mpa = null;
        if (rs.getInt("mpa_code") > 0) {
            mpa = mpaDbStorage.findPmaByCode(rs.getInt("mpa_code"));
        }

        return Film.builder()
                .id(id)
                .name(name)
                .description(description)
                .duration(duration)
                .releaseDate(releaseDate)
                .mpa(mpa)
                .directors(directors)
                .genres(genres)
                .build();
    }
}