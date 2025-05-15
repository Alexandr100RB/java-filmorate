# java-filmorate
Template repository for Filmorate project.

## ER-Диаграмма
![Диаграмма](./DBDiagram.png)

## Описание таблиц:
### users
Таблица содержит информацию о пользователях:

id: Уникальный идентификатор пользователя
email: Электронная почта пользователя
login: Логин пользователя
name: Имя пользователя
birthday: Дата рождения пользователя

### user_friends
Таблица описывает списки друзей пользователей:

user_id: Идентификатор пользователя
friend_id: Идентификатор друга
confirmed: Статус заявки в друзья

### films
Таблица содержит информацию о фильмах:

id: Уникальный идентификатор фильма
name: Название фильма
description: Описание фильма
release_date: Дата выхода фильма
duration: Продолжительность фильма
rating: Рейтинг фильма

### film_genres
Таблица содержит жанры фильмов:

film_id: Идентификатор фильма
genre: Название жанра


### likes
Таблица описывает лайки, которые пользователи ставят фильмам:

user_id: Идентификатор пользователя
film_id: Идентификатор фильма

## Пример запроса

### Получить id и название 10 самых популярных фильмов

SELECT f.id,
       f.name,
       COUNT(l.user_id) AS likes
FROM films AS f
LEFT JOIN likes AS l ON f.id = l.film_id
GROUP BY f.id, 
         f.name
ORDER BY likes DESC
LIMIT 10;
