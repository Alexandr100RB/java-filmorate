package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcOperations jdbc;

    private static Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .mpa(new Mpa(resultSet.getInt("rating_id"), resultSet.getString("mpa_rating.name")))
                .build();
    }

    private void setFilmGenres(Film film) {
        String sql = "DELETE FROM film_genres WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource("film_id", film.getId()));

        sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (:film_id, :genre_id);";
        SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(
                film.getGenres().stream().map(genre -> {
                    Map<String, Object> par = new HashMap<>();
                    par.put("film_id", film.getId());
                    par.put("genre_id", genre.getId());
                    return par;
                }).toList()
        );
        jdbc.batchUpdate(sql, batchParams);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films " +
                "(name, description, release_date, duration, rating_id)" +
                "VALUES (:name, :description, :release_date, :duration, :rating_id);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(film.toMap()), keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));
        setFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = :name, description = :description, " +
                "release_date = :release_date, duration = :duration, rating_id = :rating_id " +
                "WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource(newFilm.toMap()));
        setFilmGenres(newFilm);
        return newFilm;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "ORDER BY f.film_id";

        return jdbc.query(sql, rs -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (rs.next()) {
                if (film == null || !film.getId().equals(rs.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
                }
            }
            if (film != null) {
                films.add(film);
            }
            return films;
        });
    }

    @Override
    public Film findFilmById(Long filmId) {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "WHERE f.film_id = :film_id;";
        return jdbc.query(sql, new MapSqlParameterSource("film_id", filmId), rs -> {
            Film film = null;
            while (rs.next()) {
                if (film == null) {
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull())
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
            }
            return film;
        });
    }

    @Override
    public void deleteFilmById(Long filmId) {
        String sql = "DELETE FROM films WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource("film_id", filmId));
    }

    @Override
    public void addLikeByUser(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (:user_id, :film_id);";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("film_id", filmId);

        jdbc.update(sql, parameterSource);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {

        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN ( " +
                "    SELECT film_id, COUNT(*) AS countOfLikes " +
                "    FROM likes " +
                "    GROUP BY film_id " +
                ") AS p ON f.film_id = p.film_id " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "ORDER BY p.countOfLikes DESC, f.film_id " +
                "LIMIT :count;";

        return jdbc.query(sql, new MapSqlParameterSource("count", count), rs -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (rs.next()) {
                if (film == null || !film.getId().equals(rs.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
                }
            }
            if (film != null) {
                films.add(film);
            }
            return films;
        });
    }

    @Override
    public List<Film> getMostPopularFilms(Integer count, Integer genreId, Integer year) {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM (SELECT l.film_id, COUNT(*) AS countOfLikes " +
                "      FROM likes l " +
                "      JOIN films f ON l.film_id = f.film_id " +
                "      LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "      WHERE 1=1 " +
                (genreId != null ? " AND fg.genre_id = :genreId " : "") +
                (year != null ? " AND EXTRACT(YEAR FROM f.release_date) = :year " : "") +
                "      GROUP BY l.film_id " +
                "      ORDER BY countOfLikes DESC LIMIT :count) AS p " +
                "JOIN films AS f ON p.film_id = f.film_id " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                (genreId != null ? " WHERE fg.genre_id = :genreId " : "") +
                " ORDER BY p.countOfLikes DESC, f.film_id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);

        if (genreId != null) {
            params.addValue("genreId", genreId);
        }
        if (year != null) {
            params.addValue("year", year);
        }

        List<Film> filmsWithGenres = jdbc.query(sql, params, (rs, rowNum) -> {
            Film film = mapRowToFilm(rs, rowNum);
            Integer genreIdFromRs = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                film.getGenres().add(new Genre(genreIdFromRs, rs.getString("genre_name")));
            }
            return film;
        });

        Map<Long, Film> filmMap = new LinkedHashMap<>();
        for (Film film : filmsWithGenres) {
            filmMap.merge(film.getId(), film, (existing, current) -> {
                existing.getGenres().addAll(current.getGenres());
                return existing;
            });
        }

        return new ArrayList<>(filmMap.values());
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE user_id = :user_id AND film_id = :film_id;";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("film_id", filmId);
        jdbc.update(sql, parameterSource);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "       f.rating_id, mpa.name AS mpa_rating_name, " +
                "       g.genre_id, g.name AS genre_name " +
                "FROM films f " +
                "INNER JOIN likes l1 ON f.film_id = l1.film_id AND l1.user_id = :userId " +
                "INNER JOIN likes l2 ON f.film_id = l2.film_id AND l2.user_id = :friendId " +
                "INNER JOIN likes all_likes ON f.film_id = all_likes.film_id " +
                "LEFT JOIN mpa_rating mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, mpa.name, g.genre_id, g.name " +
                "ORDER BY COUNT(all_likes.user_id) DESC";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        List<Film> filmsWithGenres = jdbc.query(sql, params, (rs, rowNum) -> {
            Film film = mapRowToFilm(rs, rowNum);
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
            }
            return film;
        });

        Map<Long, Film> filmMap = new LinkedHashMap<>();
        for (Film film : filmsWithGenres) {
            filmMap.merge(film.getId(), film, (existing, current) -> {
                existing.getGenres().addAll(current.getGenres());
                return existing;
            });
        }

        return new ArrayList<>(filmMap.values());
    }

    @Override
    public boolean isFilmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = :film_id;";
        return 1 == jdbc.queryForObject(sql, new MapSqlParameterSource("film_id", filmId), Integer.class);
    }

    @Override
    public List<Like> getLikesForFilmsLikedByUser(long userId) {
        String sql = "SELECT * FROM likes WHERE user_id IN" +
                " (SELECT user_id FROM likes WHERE film_id IN " +
                "(SELECT film_id FROM likes WHERE user_id=(:id)))";

        return jdbc.query(sql, new MapSqlParameterSource("id", userId),
                (rs, rowNumber) -> createLike(rs));
    }

    private Like createLike(ResultSet rs) throws SQLException {
        return Like.builder()
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .build();
    }
}
