package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecommendationService {

    private final FilmStorage filmDbStorage;

    @Autowired
    public RecommendationService(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    public List<Film> getRecommendedFilms(long userId) {
        List<Like> likesForFilmsLikedByOriginUser = filmDbStorage.getLikesForFilmsLikedByUser(userId);
        if (likesForFilmsLikedByOriginUser.isEmpty()) {
            return new ArrayList<>();
        }
        HashMap<Long, Set<Long>> filmsLikedByUsers = getFilmsLikedUsers(likesForFilmsLikedByOriginUser);
        Set<Long> filmsLikedByOriginalUser = filmsLikedByUsers.get(userId);
        filmsLikedByUsers.remove(userId);
        HashMap<Long, Integer> usersWeights = getUsersWeights(filmsLikedByUsers, filmsLikedByOriginalUser);
        HashMap<Long, Integer> filmsRates = calculateFilmsRate(filmsLikedByUsers, usersWeights);
        filmsLikedByOriginalUser.forEach(filmsRates::remove);

        return getRecommendedFilms(filmsRates);
    }

    private HashMap<Long, Set<Long>> getFilmsLikedUsers(List<Like> likes) {
        HashMap<Long, Set<Long>> filmsLikedByUsers = new HashMap<>();
        likes.forEach(like -> {
            if (!filmsLikedByUsers.containsKey(like.getUserId())) {
                filmsLikedByUsers.put(like.getUserId(), new HashSet<>());
            }
            filmsLikedByUsers.get(like.getUserId()).add(like.getFilmId());
        });

        return filmsLikedByUsers;
    }

    private HashMap<Long, Integer> getUsersWeights(HashMap<Long, Set<Long>> filmsLikedByUsers,
                                                   Set<Long> filmLikedByOriginalUser) {
        HashMap<Long, Integer> usersWeights = new HashMap<>();
        for (Long userId : filmsLikedByUsers.keySet()) {
            int weight = 0;
            for (Long filmId : filmLikedByOriginalUser) {
                if (filmsLikedByUsers.get(userId).contains(filmId)) {
                    weight++;
                }
            }
            usersWeights.put(userId, weight);
        }

        return usersWeights;
    }

    private HashMap<Long, Integer> calculateFilmsRate(HashMap<Long, Set<Long>> filmsLikedBySimilarUsers,
                                                      HashMap<Long, Integer> usersWeights) {
        HashMap<Long, Integer> filmsRate = new HashMap<>();
        for (Long key : filmsLikedBySimilarUsers.keySet()) {
            for (Long value : filmsLikedBySimilarUsers.get(key)) {
                if (!filmsRate.containsKey(value)) {
                    filmsRate.put(value, usersWeights.get(key));
                } else {
                    filmsRate.put(value, filmsRate.get(value) + usersWeights.get(key));
                }
            }
        }

        return filmsRate;
    }

    private List<Film> getRecommendedFilms(HashMap<Long, Integer> filmsRate) {
        if (filmsRate.isEmpty()) {
            return new ArrayList<>();
        }
        // Сортируем ID фильмов по рейтингу
        List<Long> sortedFilmIds = filmsRate.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Получаем фильмы одним запросом
        List<Film> films = filmDbStorage.findFilmsByIds(sortedFilmIds);

        // Сохраняем порядок сортировки
        Map<Long, Film> filmById = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        List<Film> sortedFilms = sortedFilmIds.stream()
                .map(filmById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return sortedFilms;
    }
}