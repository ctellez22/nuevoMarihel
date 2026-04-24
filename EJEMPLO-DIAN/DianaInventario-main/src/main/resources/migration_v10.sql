-- Migration V10: display_id por prefijo de categoría
-- Reemplaza el contador global único por contadores por prefijo (ej: "an", "co", "ar")

-- 1. Crear la nueva tabla con clave por prefijo
CREATE TABLE IF NOT EXISTS display_id_seq_v2 (
    prefix    VARCHAR(20) PRIMARY KEY,
    last_val  BIGINT      NOT NULL DEFAULT 0
);

-- 2. Migrar el contador global existente al prefijo legado "m"
--    (para que joyas viejas con "M123" no colisionen)
INSERT INTO display_id_seq_v2 (prefix, last_val)
SELECT 'm', last_val FROM display_id_seq WHERE id = 1
ON CONFLICT (prefix) DO NOTHING;

-- 3. Inicializar prefijos por categoría a partir de los display_ids existentes
--    (para cada prefijo ya usado, tomamos el máximo número ya emitido)
INSERT INTO display_id_seq_v2 (prefix, last_val)
SELECT
    lower(substr(display_id, 1, 2))          AS prefix,
    max(cast(substr(display_id, 3) AS BIGINT)) AS last_val
FROM joya
WHERE display_id ~ '^[A-Za-z]{2}[0-9]+$'
GROUP BY lower(substr(display_id, 1, 2))
ON CONFLICT (prefix) DO UPDATE
    SET last_val = GREATEST(display_id_seq_v2.last_val, EXCLUDED.last_val);

-- 4. Renombrar tablas (la vieja queda como respaldo)
ALTER TABLE IF EXISTS display_id_seq RENAME TO display_id_seq_old;
ALTER TABLE IF EXISTS display_id_seq_v2 RENAME TO display_id_seq;

