-- =============================================================
-- Migracion v4 - Precio real de venta para joyas vendidas
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================

ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS precio_venta_real VARCHAR(255);

