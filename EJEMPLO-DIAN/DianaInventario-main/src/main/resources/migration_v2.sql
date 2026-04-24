-- =============================================================
-- Migración v2 — Agregar columnas nuevas a tabla Joya
--                y crear tablas socio y categoria
-- Ejecutar UNA SOLA VEZ contra la BD de producción/desarrollo
-- =============================================================

-- 1. Columnas nuevas en la tabla joya existente
ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS display_id VARCHAR(255) UNIQUE,
    ADD COLUMN IF NOT EXISTS socio      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fecha_ingreso TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fecha_vendida TIMESTAMP,
    ADD COLUMN IF NOT EXISTS tiene_piedra BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS info_piedra VARCHAR(255),
    ADD COLUMN IF NOT EXISTS fue_editada BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Nuevas tablas de socios y categorías


-- 2. Nueva tabla socio
CREATE TABLE IF NOT EXISTS socio (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- 3. Nueva tabla categoria
CREATE TABLE IF NOT EXISTS categoria (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

