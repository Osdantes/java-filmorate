package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, getDirectorMapper());
    }

    public Director getDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, new Object[]{id}, getDirectorMapper());
        return directors.isEmpty() ? null : directors.get(0);
    }

    public Director addDirector(Director director) {
        String sqlQuery = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, new String[] { "id" });
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return getDirectorById(director.getId());
    }

    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return getDirectorById(director.getId());
    }

    public boolean deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    public List<Director> findDirectorsByFilmId(Long filmId) {
        return jdbcTemplate.query(
                "SELECT d.* FROM film_directors fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id = ?",
                new Object[]{filmId},
                (rs, rowNum) -> new Director(rs.getInt("id"), rs.getString("name"))
        );
    }

    private RowMapper<Director> getDirectorMapper() {
        return ((rs, rowNum) -> Director.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build());
    }
}
