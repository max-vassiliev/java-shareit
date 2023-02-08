CREATE TABLE IF NOT EXISTS users
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name      VARCHAR(50)  NOT NULL,
    email     VARCHAR(100) NOT NULL,
    CONSTRAINT users_pk
        PRIMARY KEY (id),
    CONSTRAINT uq_email
        UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(500) NOT NULL,
    is_available    BOOLEAN      NOT NULL,
    owner_id        BIGINT       NOT NULL,
    CONSTRAINT items_pk
        PRIMARY KEY (id),
    CONSTRAINT item_owner_fk
        FOREIGN KEY (owner_id)
            REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY,
    starts    TIMESTAMP WITHOUT TIME ZONE,
    ends      TIMESTAMP WITHOUT TIME ZONE,
    item_id   BIGINT        NOT NULL,
    booker_id BIGINT        NOT NULL,
    status    VARCHAR(10)   NOT NULL,
    CONSTRAINT bookings_pk
        PRIMARY KEY (id),
    CONSTRAINT booker_fk
        FOREIGN KEY (booker_id)
            REFERENCES users(id),
    CONSTRAINT item_fk
        FOREIGN KEY (item_id)
            REFERENCES items(id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY,
    text      VARCHAR(800) NOT NULL,
    item_id   BIGINT       NOT NULL,
    author_id BIGINT       NOT NULL,
    created   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT comments_pk
        PRIMARY KEY (id),
    CONSTRAINT comments_items_fk
        FOREIGN KEY (item_id)
            REFERENCES items ON DELETE CASCADE,
    CONSTRAINT comments_users_fk
        FOREIGN KEY (author_id)
            REFERENCES users ON DELETE CASCADE
);