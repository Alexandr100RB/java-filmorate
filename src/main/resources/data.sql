delete from mpa_rating;
delete from genres;
delete from film_genres;
delete from films;
delete from likes;
delete from directors;
delete from film_directors;

insert into mpa_rating(rating_id, name)
values (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');

insert into genres(genre_id, name)
values (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

insert into event_types(event_type_id, event_name)
values (1, 'LIKE'),
    (2, 'REVIEW'),
    (3, 'FRIEND');

insert into operations_types(operation_type_id, operation_name)
values (1, 'ADD'),
    (2, 'REMOVE'),
    (3, 'UPDATE');