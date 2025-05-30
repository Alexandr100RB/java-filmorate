package ru.yandex.practicum.filmorate.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(@Valid Review review) {
        validateReviewFields(review, false);

        try {
            userStorage.getUserById(review.getUserId());
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException("User with id " + review.getUserId() + " not found");
        }

        Film film = filmStorage.findFilmById(review.getFilmId());
        if (film == null) {
            throw new DataNotFoundException("Film with id " + review.getFilmId() + " not found");
        }

        return reviewStorage.create(review);
    }


    public Review update(@Valid Review review) {
        validateReviewFields(review, true);

        Review existingReview = reviewStorage.findById(review.getReviewId())
                .orElseThrow(() -> new DataNotFoundException("Review with id " + review.getReviewId() + " not found"));

        if (!existingReview.getUserId().equals(review.getUserId()) ||
                !existingReview.getFilmId().equals(review.getFilmId())) {
            throw new IllegalArgumentException("Cannot change userId or filmId of review");
        }

        reviewStorage.update(review);

        return reviewStorage.findById(review.getReviewId())
                .orElseThrow(() -> new DataNotFoundException("Review with id " + review.getReviewId() + " not found after update"));
    }

    public void delete(int id) {
        reviewStorage.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Review with id " + id + " not found"));

        reviewStorage.delete(id);
    }

    public Review findById(int id) {
        return reviewStorage.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Review with id " + id + " not found"));
    }

    public List<Review> findByFilmId(Long filmId, Integer count) {
        int finalCount = (count == null || count < 0) ? 10 : count;
        return reviewStorage.findByFilmId(filmId, finalCount);
    }

    public void addLike(int reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeReaction(int reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewStorage.removeReaction(reviewId, userId);
    }

    private void validateReviewFields(Review review, boolean isUpdate) {
        if (isUpdate && (review.getReviewId() == null)) {
            throw new IllegalArgumentException("Review ID must not be null for update");
        }
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Review content must not be blank");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("Review isPositive must not be null");
        }
        if (review.getUserId() == null) {
            throw new IllegalArgumentException("Review userId must not be null");
        }
        if (review.getFilmId() == null) {
            throw new IllegalArgumentException("Review filmId must not be null");
        }
    }

    private void validateReviewAndUser(int reviewId, Long userId) {
        if (reviewStorage.findById(reviewId) == null) {
            throw new DataNotFoundException("Review with id " + reviewId + " not found");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new DataNotFoundException("User with id " + userId + " not found");
        }
    }

}

