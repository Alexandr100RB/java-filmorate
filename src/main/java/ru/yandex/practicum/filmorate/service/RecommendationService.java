package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.*;

@Slf4j
@Service
public class RecommendationService {

    private final FilmStorage filmDbStorage;

    @Autowired
    public RecommendationService(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    public List<Film> getRecommendedFilmsByRate(long userId) {
        List<Like> likesForUserFilms = filmDbStorage.getLikesForFilmsLikedByUser(userId);
        if (likesForUserFilms.isEmpty()) {
            return new ArrayList<>();
        }
        HashMap<Long, Set<Long>> filmsLikedByUsers = getFilmsLikedUsers(likesForUserFilms);
        Set<Long> filmsLikedByOriginalUser = filmsLikedByUsers.get(userId);
        filmsLikedByUsers.remove(userId);
        HashMap<Long, Integer> usersWeights = getUsersWeights(filmsLikedByUsers, filmsLikedByOriginalUser);
        HashMap<Long, Integer> filmsRates = calculateFilmsRate(filmsLikedByUsers, usersWeights);
        for (Long filmId : filmsLikedByOriginalUser) {
            filmsRates.remove(filmId);
        }

        return getRecommendedFilmsByRate(filmsRates);
    }

    private HashMap<Long, Set<Long>> getFilmsLikedUsers(List<Like> likesSet) {
        HashMap<Long, Set<Long>> filmsLikedByUsers = new HashMap<>();
        for (Like like : likesSet) {
            if (!filmsLikedByUsers.containsKey(like.getUserId())) {
                filmsLikedByUsers.put(like.getUserId(), new HashSet<>());
            }
            filmsLikedByUsers.get(like.getUserId()).add(like.getFilmId());
        }

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

    private List<Film> getRecommendedFilmsByRate(HashMap<Long, Integer> filmsRate) {
        List<Film> films = new ArrayList<>();
        while (!filmsRate.isEmpty()) {
            Long flmId = Collections.max(filmsRate.entrySet(), Map.Entry.comparingByValue()).getKey();
            Film film = filmDbStorage.findFilmById(flmId);
            films.add(film);
            filmsRate.remove(flmId);
        }

        return films;
    }
}