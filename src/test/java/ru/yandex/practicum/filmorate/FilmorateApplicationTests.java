package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, GenreDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final GenreDbStorage genreStorage;
	private final FilmDbStorage filmStorage;
	private final MpaDbStorage mpaStorage;


	@BeforeEach
	void setUp() {
		userStorage.create(User.builder()
				.id(1L)
				.email("test@email.ru")
				.login("Test login")
				.name("Test name")
				.birthday(LocalDate.parse("2001-02-14"))
				.build());

		filmStorage.create(Film.builder()
				.id(1L)
				.name("Test name")
				.description("Test description")
				.releaseDate(LocalDate.parse("1980-05-21"))
				.duration(100)
				.mpa(new Mpa(1, "G"))
				.build());
	}

	@Test
	void getAll() {
		assertThat(filmStorage.findAll())
				.hasSize((int) 1);
	}
}
