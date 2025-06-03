package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Validated
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAll() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director get(@PathVariable("id") int directorId) {
        return directorService.getDirectorById(directorId);
    }

    @PostMapping
    public Director create(@Validated @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director newDirector) {
        return directorService.update(newDirector);
    }

    @DeleteMapping("/{id}")
    public void deleteFriend(@PathVariable("id") int directorId) {
        directorService.deleteDirectorById(directorId);
    }
}