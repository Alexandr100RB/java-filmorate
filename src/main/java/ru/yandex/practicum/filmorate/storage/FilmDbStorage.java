package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Like;

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
    private final MpaStorage mpaStorage;

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Long id = resultSet.getLong("film_id");
        Film film = new Film();
        film.setId(id);
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));
        film.setMpa(mpaStorage.getMpaById(resultSet.getInt("rating_id")));
        film.setDirectors(loadDirectors(id));
        return film;
    }

    @Override
    public void setFilmGenres(Long filmId, Set<Genre> genres) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = :filmId", Map.of("filmId", filmId));
        if (genres != null) {
            for (Genre genre : genres) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (:filmId, :genreId)",
                        Map.of("filmId", filmId, "genreId", genre.getId()));
            }
        }
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films " +
                "(name, description, release_date, duration, rating_id)" +
                "VALUES (:name, :description, :releaseDate, :duration, :ratingId);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("ratingId", film.getMpa().getId());
        jdbc.update(sql, params, keyHolder, new String[]{"film_id"});
        film.setId(keyHolder.getKeyAs(Long.class));
        setFilmGenres(film.getId(), film.getGenres());
        setFilmDirectors(film.getId(), film.getDirectors());

        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = :name, description = :description, " +
                "release_date = :release_date, duration = :duration, rating_id = :rating_id " +
                "WHERE film_id = :film_id";
        Map<String, Object> params = Map.of(
                "film_id", newFilm.getId(),
                "name", newFilm.getName(),
                "description", newFilm.getDescription(),
                "release_date", newFilm.getReleaseDate(),
                "duration", newFilm.getDuration(),
                "rating_id", newFilm.getMpa().getId()
        );
        jdbc.update(sql, params);

        setFilmGenres(newFilm.getId(), newFilm.getGenres());
        setFilmDirectors(newFilm.getId(), newFilm.getDirectors());
        return newFilm;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, " +
                "genres.genre_id, genres.name AS genre_name, " +
                "directors.director_id, directors.name AS director_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id " +
                "LEFT JOIN directors ON fd.director_id = directors.director_id " +
                "ORDER BY f.film_id";

        return jdbc.query(sql, resultSet -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (resultSet.next()) {
                if (film == null || !film.getId().equals(resultSet.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(resultSet, resultSet.getRow());
                }
                Integer genreId = resultSet.getInt("genre_id");
                if (!resultSet.wasNull()) {
                    film.getGenres().add(new Genre(genreId, resultSet.getString("genre_name")));
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
        return jdbc.query(sql, new MapSqlParameterSource("film_id", filmId), resultSet -> {
            Film film = null;
            while (resultSet.next()) {
                if (film == null) {
                    film = mapRowToFilm(resultSet, resultSet.getRow());
                }
                Integer genreId = resultSet.getInt("genre_id");
                if (!resultSet.wasNull())
                    film.getGenres().add(new Genre(genreId, resultSet.getString("genre_name")));
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

    public List<Film> getFilmsByDirectorSorted(int directorId, String sortBy) {
        String sql;

        if ("likes".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.* FROM films f " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "WHERE fd.director_id = :directorId " +
                    "GROUP BY f.film_id " +
                    "ORDER BY COUNT(l.user_id) DESC";
        } else if ("year".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.* FROM films f " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = :directorId " +
                    "ORDER BY f.release_date";
        } else {
            throw new IllegalArgumentException("Invalid sort parameter: " + sortBy);
        }

        return jdbc.query(sql, Map.of("directorId", directorId), (rs, rowNum) -> {
            Film film = mapRowToFilm(rs, 2); // твой метод маппинга фильма
            film.setDirectors(loadDirectors(film.getId()));
            return film;
        });
    }

    @Override
    public void setFilmDirectors(long filmId, Set<Director> directors) {
        jdbc.update("DELETE FROM film_directors WHERE film_id = :filmId", Map.of("filmId", filmId));
        if (directors != null) {
            for (Director director : directors) {
                jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (:filmId, :directorId)",
                        Map.of("filmId", filmId, "directorId", director.getId()));
            }
        }
    }

    @Override
    public Set<Director> loadDirectors(Long filmId) {
        String sql = "SELECT d.director_id, d.name FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = :filmId";

        return new HashSet<>(jdbc.query(sql, Map.of("filmId", filmId),
                (rs, rowNum) -> new Director(rs.getInt("director_id"), rs.getString("name"))));
    }

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