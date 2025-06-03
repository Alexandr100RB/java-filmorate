package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventTypes;
import ru.yandex.practicum.filmorate.model.enums.OperationTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public void addFeed(long userId, long entityId, long eventTypeId, long operationTypeId, long timestamp) {

        String sql = "INSERT INTO feeds " +
                "(user_id, entity_id, event_type_id, operation_type_id, timestamp)" +
                "VALUES (:userId, :enttityId, :eventTypeId, :operationTypeId, :timestamp);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(Map.of("userId", userId,
                "enttityId", entityId, "eventTypeId", eventTypeId,
                "operationTypeId", operationTypeId, "timestamp", timestamp)), keyHolder);
    }

    @Override
    public List<Feed> findFeedsForUser(Long userId) {
        String sql = "SELECT * FROM feeds AS f JOIN event_types AS t ON f.event_type_id = t.event_type_id " +
                "JOIN operations_types AS o ON f.operation_type_id = o.operation_type_id WHERE user_id = :userId";

        return jdbc.query(sql, new MapSqlParameterSource(Map.of("userId", userId)), this::mapRow);
    }

    @Override
    public Optional<Long> findOEventTypeIdByName(EventTypes eventType) {
        String sql = "SELECT event_type_id FROM event_types WHERE event_name = :eventType";
        try {
            Long result = jdbc.queryForObject(sql,
                    new MapSqlParameterSource(Map.of("eventType", eventType.toString())), Long.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> findOperationTypeIdByName(OperationTypes operationType) {
        String sql = "SELECT operation_type_id FROM operations_types WHERE operation_name = :operationType";
        try {
            Long result = jdbc.queryForObject(sql,
                    new MapSqlParameterSource(Map.of("operationType", operationType.toString())), Long.class);
            return Optional.ofNullable(result);
        } catch (
                EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long eventId = rs.getLong("event_id");
        Long userId = rs.getLong("user_id");
        Long entityId = rs.getLong("entity_id");
        Long timestamp = rs.getLong("timestamp");
        String eventType = rs.getString("event_name");
        String operation = rs.getString("operation_name");

        return new Feed(eventId, userId, entityId, EventTypes.valueOf(eventType), OperationTypes.valueOf(operation), timestamp);
    }
}