-- =============================================================
-- Script de creación de tablas para la base de datos PostgreSQL
-- Proyecto: Diana Inventario
-- Fecha: 2026-04-02
-- Ejecutar en psql o pgAdmin contra la BD del proyecto
-- =============================================================

-- -------------------------------------------------------
-- Tabla: socio
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS socio (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- -------------------------------------------------------
-- Tabla: joyero
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS joyero (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

-- -------------------------------------------------------
-- Tabla: categoria
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

-- -------------------------------------------------------
-- Tabla: categoria_verificacion
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria_verificacion (
    id                        BIGSERIAL    PRIMARY KEY,
    nombre_categoria          VARCHAR(255) NOT NULL,
    ultima_fecha_verificacion TIMESTAMP
);

-- -------------------------------------------------------
-- Tabla: joya
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS joya (
    id           BIGSERIAL    PRIMARY KEY,
    display_id   VARCHAR(255) UNIQUE,
    nombre       VARCHAR(255),
    precio       VARCHAR(255),
    peso         DOUBLE PRECISION NOT NULL DEFAULT 0,
    categoria    VARCHAR(255),
    observacion  VARCHAR(255),
    tiene_piedra BOOLEAN      NOT NULL DEFAULT FALSE,
    info_piedra  VARCHAR(255),
    fue_editada  BOOLEAN      NOT NULL DEFAULT FALSE,
    vendido      BOOLEAN      NOT NULL DEFAULT FALSE,
    precio_venta_real VARCHAR(255),
    estado       VARCHAR(50)  DEFAULT 'disponible',
    socio        VARCHAR(255),
    punto_fisico VARCHAR(255),
    fecha_ingreso  TIMESTAMP,
    fecha_vendida  TIMESTAMP
);

-- -------------------------------------------------------
-- Seguridad: roles y usuarios
-- -------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_rol (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS app_usuario (
    id             BIGSERIAL    PRIMARY KEY,
    username       VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    punto_fisico   VARCHAR(255),
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

-- -------------------------------------------------------
-- Aprobaciones
-- -------------------------------------------------------
ALTER TABLE joya
    ADD COLUMN IF NOT EXISTS precio_venta_real VARCHAR(255),
    ADD COLUMN IF NOT EXISTS autorizado   BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255),
    ADD COLUMN IF NOT EXISTS actualizado_en TIMESTAMP,
    ADD COLUMN IF NOT EXISTS actualizado_por BIGINT;


ALTER TABLE app_usuario
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255);

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

-- -------------------------------------------------------
-- Tabla: lote
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS lote (
    id                  BIGSERIAL    PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    peso_total          DOUBLE PRECISION NOT NULL,
    cantidad_piedras    INTEGER      NOT NULL,
    tipo_piedra         VARCHAR(100) NOT NULL,
    calidad_piedra      VARCHAR(100),
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT NOW(),
    descripcion         TEXT,
    precio_estimado     VARCHAR(50),
    precio_venta_real   VARCHAR(50),
    socio               VARCHAR(255),
    punto_fisico        VARCHAR(255),
    categoria           VARCHAR(100),
    estado              VARCHAR(50)  NOT NULL DEFAULT 'disponible',
    fecha_venta         TIMESTAMP,
    vendido             BOOLEAN      DEFAULT FALSE,
    observaciones       TEXT,
    autorizado          BOOLEAN      NOT NULL DEFAULT TRUE,
    actualizado_en      TIMESTAMP,
    actualizado_por     BIGINT,
    CONSTRAINT fk_lote_actualizado_por FOREIGN KEY (actualizado_por) REFERENCES app_usuario (id)
);

-- -------------------------------------------------------
-- Tabla: orden_trabajo
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS orden_trabajo (
    id             BIGSERIAL PRIMARY KEY,
    joya_id        BIGINT       NOT NULL,
    joya_display_id VARCHAR(255),
    joya_nombre    VARCHAR(255),
    joyero         VARCHAR(255) NOT NULL,
    fecha_envio    TIMESTAMP    NOT NULL,
    fecha_entrega  TIMESTAMP    NOT NULL,
    detalle        TEXT,
    estado         VARCHAR(50)  NOT NULL DEFAULT 'pendiente',
    creado_en      TIMESTAMP    NOT NULL DEFAULT NOW(),
    punto_fisico   VARCHAR(255),
    CONSTRAINT fk_orden_trabajo_joya FOREIGN KEY (joya_id) REFERENCES joya (id)
);

CREATE INDEX IF NOT EXISTS idx_orden_trabajo_joya ON orden_trabajo (joya_id);
CREATE INDEX IF NOT EXISTS idx_orden_trabajo_estado ON orden_trabajo (estado);

ALTER TABLE lote
    ADD COLUMN IF NOT EXISTS punto_fisico VARCHAR(255);

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

-- Catálogo mínimo de roles
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

