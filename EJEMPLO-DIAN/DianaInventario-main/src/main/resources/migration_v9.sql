-- Migration V9: catalogo de joyeros

CREATE TABLE IF NOT EXISTS joyero (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

