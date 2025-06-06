package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Director getDirectorById(Integer directorId) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = :id";
        List<Director> result = jdbc.query(sqlQuery, Map.of("id", directorId), this::mapRowToDirector);
        return result.getFirst();
    }

    @Override
    public Collection<Director> findAll() {
        String sqlQuery = "SELECT * FROM directors ORDER BY director_id";
        return jdbc.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public boolean isDirectorExists(Integer directorId) {
        String sqlQuery = "SELECT COUNT(*) FROM directors WHERE director_id = :id";
        Integer count = jdbc.queryForObject(sqlQuery, Map.of("id", directorId), Integer.class);
        return count != null && count > 0;

    }

    @Override
    public void deleteDirectorById(int directorId) {
        String deleteLinksSql = "DELETE FROM film_directors WHERE director_id = :id";
        String deleteDirectorSql = "DELETE FROM directors WHERE director_id = :id";
        Map<String, Object> params = Map.of("id", directorId);
        jdbc.update(deleteLinksSql, params);
        jdbc.update(deleteDirectorSql, params);
    }

    @Override
    public Director create(Director director) {
        String sqlQuery = "INSERT INTO directors (name) VALUES (:name)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("name", director.getName());
        jdbc.update(sqlQuery, params, keyHolder, new String[]{"director_id"});
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sqlQuery = "UPDATE directors SET name = :name WHERE director_id = :id";
        Map<String, Object> params = Map.of(
                "name", director.getName(),
                "id", director.getId()
        );
        jdbc.update(sqlQuery, params);
        return director;
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return new Director(
                resultSet.getInt("director_id"),
                resultSet.getString("name")
        );
    }
}
