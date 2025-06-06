DELETE FROM feeds;
DELETE FROM review_reactions;
DELETE FROM reviews;
DELETE FROM likes;
DELETE FROM friendship;
DELETE FROM users;
DELETE FROM film_directors;
DELETE FROM directors;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM genres;
DELETE FROM mpa_rating;
DELETE FROM event_types;
DELETE FROM operations_types;

INSERT INTO mpa_rating (rating_id, name) VALUES
  (1, 'G'),
  (2, 'PG'),
  (3, 'PG-13'),
  (4, 'R'),
  (5, 'NC-17');

INSERT INTO genres (genre_id, name) VALUES
  (1, 'Комедия'),
  (2, 'Драма'),
  (3, 'Мультфильм'),
  (4, 'Триллер'),
  (5, 'Документальный'),
  (6, 'Боевик');

INSERT INTO event_types (event_type_id, event_name) VALUES
  (1, 'LIKE'),
  (2, 'REVIEW'),
  (3, 'FRIEND');

INSERT INTO operations_types (operation_type_id, operation_name) VALUES
  (1, 'ADD'),
  (2, 'REMOVE'),
  (3, 'UPDATE');
