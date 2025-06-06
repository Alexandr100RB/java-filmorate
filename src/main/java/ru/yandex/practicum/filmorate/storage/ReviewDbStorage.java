package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (:content, :isPositive, :userId, :filmId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("userId", review.getUserId())
                .addValue("filmId", review.getFilmId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"review_id"});

        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = :content, is_positive = :isPositive WHERE review_id = :reviewId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive())
                .addValue("reviewId", review.getReviewId());

        int updated = jdbc.update(sql, params);
        if (updated == 0) {
            throw new DataNotFoundException("Отзыв с id " + review.getReviewId() + " не найден");
        }
        return findById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(Integer id) {
        jdbc.update("DELETE FROM reviews WHERE review_id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Review> findById(Integer id) {
        String sql = "SELECT * FROM reviews WHERE review_id = :id";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql,
                    new MapSqlParameterSource("id", id),
                    this::mapRowToReview));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = (filmId == null)
                ? "SELECT * FROM reviews ORDER BY useful DESC LIMIT :count"
                : "SELECT * FROM reviews WHERE film_id = :filmId ORDER BY useful DESC LIMIT :count";

        MapSqlParameterSource params = new MapSqlParameterSource("count", count);
        if (filmId != null) {
            params.addValue("filmId", filmId);
        }

        return jdbc.query(sql, params, this::mapRowToReview);
    }

    @Override
    public void addLike(Integer reviewId, Long userId) {
        updateReaction(reviewId, userId, true);
    }

    @Override
    public void addDislike(Integer reviewId, Long userId) {
        updateReaction(reviewId, userId, false);
    }

    @Override
    public void removeReaction(Integer reviewId, Long userId) {
        String sql = "DELETE FROM review_reactions WHERE review_id = :reviewId AND user_id = :userId";
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId));
        recalculateUseful(reviewId);
    }

    private void updateReaction(Integer reviewId, Long userId, boolean isLike) {
        String checkSql = "SELECT COUNT(*) FROM review_reactions WHERE review_id = :reviewId AND user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userId", userId)
                .addValue("isLike", isLike); // заранее добавим, пригодится

        Integer count = jdbc.queryForObject(checkSql, params, Integer.class);

        if (count != null && count > 0) {
            String updateSql = "UPDATE review_reactions SET is_like = :isLike WHERE review_id = :reviewId AND user_id = :userId";
            jdbc.update(updateSql, params);
        } else {
            String insertSql = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (:reviewId, :userId, :isLike)";
            jdbc.update(insertSql, params);
        }

        recalculateUseful(reviewId);
    }


    private void recalculateUseful(Integer reviewId) {
        String sql = "SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) " +
                "FROM review_reactions WHERE review_id = :reviewId";
        Integer useful = jdbc.queryForObject(sql, new MapSqlParameterSource("reviewId", reviewId), Integer.class);

        jdbc.update("UPDATE reviews SET useful = :useful WHERE review_id = :reviewId",
                new MapSqlParameterSource()
                        .addValue("useful", useful)
                        .addValue("reviewId", reviewId));
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}




