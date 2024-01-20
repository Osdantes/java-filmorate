package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FeedDbStorage implements FeedStorage {
    private static final String INSERT_SQL =
            "insert into events(timestamp, operation, event_type, user_id, entity_id) values(?, ?, ?, ?, ?)";
    private final JdbcTemplate jdbcTemplate;

    private void addFeed(EventType eventType, long userId, long entityId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            stmt.setLong(1, Instant.now().toEpochMilli());
            stmt.setString(2, Operation.ADD.toString());
            stmt.setString(3, eventType.toString());
            stmt.setLong(4, userId);
            stmt.setLong(5, entityId);
            return stmt;
        }, keyHolder);

        log.info("User {} has added {} {} with object {}.",
                userId, keyHolder.getKey().longValue(), eventType, entityId);
    }

    private void removeFeed(EventType eventType, long userId, long entityId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            stmt.setLong(1, Instant.now().toEpochMilli());
            stmt.setString(2, Operation.REMOVE.toString());
            stmt.setString(3, eventType.toString());
            stmt.setLong(4, userId);
            stmt.setLong(5, entityId);
            return stmt;
        }, keyHolder);

        log.info("User {} has removed {} {} with object {}.",
                userId, keyHolder.getKey().longValue(), eventType, entityId);
    }

    private void updateFeed(EventType eventType, long userId, long entityId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            stmt.setLong(1, Instant.now().toEpochMilli());
            stmt.setString(2, Operation.UPDATE.toString());
            stmt.setString(3, eventType.toString());
            stmt.setLong(4, userId);
            stmt.setLong(5, entityId);
            return stmt;
        }, keyHolder);

        log.info("User {} has updated {} {} with object {}.",
                userId, keyHolder.getKey().longValue(), eventType, entityId);
    }

    @Override
    public void addReview(long userId, long entityId) {
        addFeed(EventType.REVIEW, userId, entityId);
    }

    @Override
    public void deleteReview(long userId, long entityId) {
        removeFeed(EventType.REVIEW, userId, entityId);
    }

    @Override
    public void updateReview(long userId, long entityId) {
        updateFeed(EventType.REVIEW, userId, entityId);
    }

    @Override
    public void addLike(long userId, long entityId) {
        addFeed(EventType.LIKE, userId, entityId);
    }

    @Override
    public void deleteLike(long userId, long entityId) {
        removeFeed(EventType.LIKE, userId, entityId);
    }

    @Override
    public void addFriendRequest(long userId, long entityId) {
        addFeed(EventType.FRIEND, userId, entityId);
    }

    @Override
    public void deleteFriendRequest(long userId, long entityId) {
        removeFeed(EventType.FRIEND, userId, entityId);
    }

    @Override
    public void acceptFriendRequest(long userId, long entityId) {
        updateFeed(EventType.FRIEND, userId, entityId);
    }

    @Override
    public List<Feed> getFeed(long userId) {
        String sql = "select e.* " +
                "from events e " +
                "where e.user_id = ? " +
                "order by e.id asc";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapper(rs), userId);
    }

    private Feed mapper(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        long timestamp = resultSet.getLong("timestamp");
        Operation operation = Operation.valueOf(resultSet.getString("operation"));
        EventType eventType = EventType.valueOf(resultSet.getString("event_type"));
        long userId = resultSet.getLong("user_id");
        long entityId = resultSet.getLong("entity_id");

        return Feed.builder()
                .eventId(id)
                .timestamp(timestamp)
                .operation(operation)
                .eventType(eventType)
                .userId(userId)
                .entityId(entityId)
                .build();
    }
}