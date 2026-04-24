-- =============================================================
-- Migracion v6 - Campo punto_fisico para vendedores (app_usuario), joyas y lotes
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================


ALTER TABLE app_usuario
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255);

ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255);

ALTER TABLE lote
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255);

