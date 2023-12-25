DROP ALL OBJECTS;

CREATE TABLE IF NOT EXISTS users (
  id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar,
  login varchar,
  email varchar,
  birthday date
);
CREATE UNIQUE INDEX IF NOT EXISTS users$id ON users (id);

CREATE TABLE IF NOT EXISTS friends_status_dic (
  status_code integer PRIMARY KEY,
  status varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS friends_link (
  request_user_id integer REFERENCES users (id) ON DELETE CASCADE,
  accept_user_id integer REFERENCES users (id) ON DELETE CASCADE,
  status_code integer REFERENCES friends_status_dic (status_code) ON DELETE SET NULL,
  PRIMARY KEY(request_user_id, accept_user_id)
);
CREATE INDEX IF NOT EXISTS friends_link$rid ON friends_link (request_user_id);
CREATE INDEX IF NOT EXISTS friends_link$aid ON friends_link (accept_user_id);
                

CREATE TABLE IF NOT EXISTS mpa_dic (
  mpa_code integer PRIMARY KEY,
  mpa varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
  id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  name varchar,
  description text,
  duration integer,
  mpa_code integer REFERENCES mpa_dic (mpa_code) ON DELETE SET NULL,
  release_date date
);
CREATE UNIQUE INDEX IF NOT EXISTS films$id ON films (id);

CREATE TABLE IF NOT EXISTS likes_link (
  user_id integer REFERENCES users (id) ON DELETE CASCADE,
  film_id integer REFERENCES films (id) ON DELETE CASCADE,
  PRIMARY KEY(user_id, film_id)
);
CREATE INDEX IF NOT EXISTS likes_link$uid ON likes_link (user_id);
CREATE INDEX IF NOT EXISTS likes_link$fid ON likes_link (film_id);

CREATE TABLE IF NOT EXISTS genre_dic (
  genre_code integer PRIMARY KEY,
  genre varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS genre_link (
  film_id integer REFERENCES films (id) ON DELETE CASCADE,
  genre_code integer REFERENCES genre_dic (genre_code) ON DELETE SET NULL,
  PRIMARY KEY(film_id, genre_code)
);
CREATE INDEX IF NOT EXISTS genre_link$fid ON genre_link (film_id);