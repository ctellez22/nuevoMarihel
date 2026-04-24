-- =============================================================
-- Migracion v5 - Credenciales iniciales de acceso
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO app_rol (nombre)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM app_rol WHERE nombre = 'ADMIN');

INSERT INTO app_rol (nombre)
SELECT 'VENDEDOR'
WHERE NOT EXISTS (SELECT 1 FROM app_rol WHERE nombre = 'VENDEDOR');

-- Credenciales iniciales:
-- admin / Admin123!
-- vendedor / Vendedor123!
INSERT INTO app_usuario (username, password_hash, activo)
SELECT 'admin', crypt('Admin123!', gen_salt('bf', 10)), TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_usuario WHERE username = 'admin');

INSERT INTO app_usuario (username, password_hash, activo)
SELECT 'vendedor', crypt('Vendedor123!', gen_salt('bf', 10)), TRUE
WHERE NOT EXISTS (SELECT 1 FROM app_usuario WHERE username = 'vendedor');

INSERT INTO app_usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM app_usuario u
JOIN app_rol r ON r.nombre = 'ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

INSERT INTO app_usuario_rol (usuario_id, rol_id)
SELECT u.id, r.id
FROM app_usuario u
JOIN app_rol r ON r.nombre = 'VENDEDOR'
WHERE u.username = 'vendedor'
ON CONFLICT DO NOTHING;

