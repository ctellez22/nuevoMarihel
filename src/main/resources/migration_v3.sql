-- =============================================================
-- Migracion v3 - Seguridad y flujo de aprobacion
-- Ejecutar UNA SOLA VEZ en ambientes existentes
-- =============================================================

-- -------------------------------------------------------
-- Seguridad: roles y usuarios
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS app_rol (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS app_usuario (
    id             BIGSERIAL    PRIMARY KEY,
    username       VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ultimo_login   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_usuario_rol (
    usuario_id BIGINT NOT NULL,
    rol_id     BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_app_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES app_usuario (id),
    CONSTRAINT fk_app_usuario_rol_rol FOREIGN KEY (rol_id) REFERENCES app_rol (id)
);

INSERT INTO app_rol (nombre)
SELECT 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM app_rol WHERE nombre = 'ADMIN');

INSERT INTO app_rol (nombre)
SELECT 'VENDEDOR'
WHERE NOT EXISTS (SELECT 1 FROM app_rol WHERE nombre = 'VENDEDOR');

-- -------------------------------------------------------
-- Cambios de autorizacion para joya
-- -------------------------------------------------------
ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS autorizado     BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_joya_actualizado_por'
          AND table_name = 'joya'
    ) THEN
        ALTER TABLE joya
            ADD CONSTRAINT fk_joya_actualizado_por
                FOREIGN KEY (actualizado_por) REFERENCES app_usuario (id);
    END IF;
END
$$;

-- -------------------------------------------------------
-- Cola de aprobaciones y auditoria
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS cambio_pendiente (
    id             BIGSERIAL    PRIMARY KEY,
    entidad        VARCHAR(50)  NOT NULL,
    entidad_id     BIGINT,
    operacion      VARCHAR(10)  NOT NULL,
    before_json    JSONB,
    after_json     JSONB,
    estado         VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE',
    solicitado_por BIGINT       NOT NULL,
    solicitado_en  TIMESTAMP    NOT NULL DEFAULT NOW(),
    revisado_por   BIGINT,
    revisado_en    TIMESTAMP,
    comentario     VARCHAR(500),
    CONSTRAINT chk_cambio_operacion CHECK (operacion IN ('INSERT', 'UPDATE', 'DELETE')),
    CONSTRAINT chk_cambio_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    CONSTRAINT fk_cambio_solicitado_por FOREIGN KEY (solicitado_por) REFERENCES app_usuario (id),
    CONSTRAINT fk_cambio_revisado_por FOREIGN KEY (revisado_por) REFERENCES app_usuario (id)
);

CREATE INDEX IF NOT EXISTS idx_cambio_pendiente_estado_fecha
    ON cambio_pendiente (estado, solicitado_en);

CREATE TABLE IF NOT EXISTS auditoria_evento (
    id            BIGSERIAL    PRIMARY KEY,
    usuario_id    BIGINT       NOT NULL,
    accion        VARCHAR(50)  NOT NULL,
    entidad       VARCHAR(50),
    entidad_id    BIGINT,
    detalle_json  JSONB,
    fecha_evento  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES app_usuario (id)
);

-- -------------------------------------------------------
-- Ejemplos de carga inicial (descomentar y ajustar hash)
-- hash sugerido: BCrypt generado fuera de la app.
-- -------------------------------------------------------
-- INSERT INTO app_usuario (username, password_hash)
-- VALUES ('admin', '$2a$10$REEMPLAZAR_HASH_REAL')
-- ON CONFLICT (username) DO NOTHING;
--
-- INSERT INTO app_usuario_rol (usuario_id, rol_id)
-- SELECT u.id, r.id
-- FROM app_usuario u
-- JOIN app_rol r ON r.nombre = 'ADMIN'
-- WHERE u.username = 'admin'
-- ON CONFLICT DO NOTHING;

