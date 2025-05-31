package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Integer id);

    Optional<Review> findById(Integer id);

    List<Review> findByFilmId(Long filmId, int count);

    void addLike(Integer reviewId, Long userId);

    void addDislike(Integer reviewId, Long userId);

    void removeReaction(Integer reviewId, Long userId);
}

