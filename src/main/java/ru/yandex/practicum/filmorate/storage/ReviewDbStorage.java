package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            review.setReviewId(key.intValue());
        }
        review.setUseful(0);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (rowsUpdated == 0) {
            throw new DataNotFoundException("Review with id " + review.getReviewId() + " not found");
        }
        return findById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id);
    }

    @Override
    public Optional<Review> findById(Integer id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToReview, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = (filmId == null)
                ? "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?"
                : "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

        return (filmId == null)
                ? jdbcTemplate.query(sql, this::mapRowToReview, count)
                : jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
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
        jdbcTemplate.update(
                "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ?",
                reviewId, userId);
        recalculateUseful(reviewId);
    }

    private void updateReaction(Integer reviewId, Long userId, boolean isLike) {
        jdbcTemplate.update(
                "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (?, ?, ?) " +
                        "ON CONFLICT (review_id, user_id) DO UPDATE SET is_like = ?",
                reviewId, userId, isLike, isLike);
        recalculateUseful(reviewId);
    }

    private void recalculateUseful(Integer reviewId) {
        Integer useful = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) " +
                        "FROM review_reactions WHERE review_id = ?",
                Integer.class, reviewId);

        jdbcTemplate.update(
                "UPDATE reviews SET useful = ? WHERE review_id = ?",
                useful, reviewId);
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



