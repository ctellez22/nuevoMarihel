-- =============================================================
-- Migracion v7 - Socio sin punto_fisico
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================

ALTER TABLE socio
    DROP COLUMN IF EXISTS punto_fisico;

