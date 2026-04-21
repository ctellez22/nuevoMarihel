-- =============================================================
-- Migracion v4 - Precio real de venta en joyas
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================

ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS precio_venta VARCHAR(255);

