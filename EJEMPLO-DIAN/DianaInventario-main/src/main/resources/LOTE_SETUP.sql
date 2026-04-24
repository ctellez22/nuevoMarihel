-- ============================================================
-- Script para crear y probar la tabla LOTE
-- Ejecutar este script después de ejecutar schema.sql
-- ============================================================

-- 1. Crear la tabla lote (si no existe)
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

-- 2. Crear índice para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_lote_estado ON lote(estado);
CREATE INDEX IF NOT EXISTS idx_lote_fecha_creacion ON lote(fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_lote_socio ON lote(socio);

-- 3. Datos de prueba (opcional)
-- Descomenta las siguientes líneas para insertar datos de prueba
/*
INSERT INTO lote (nombre, peso_total, cantidad_piedras, tipo_piedra, calidad_piedra, descripcion, precio_estimado, socio, categoria, observaciones, estado)
VALUES (
    'Lote Diamantes Premium',
    150.5,
    25,
    'Diamante',
    'Premium',
    'Conjunto de diamantes de alta calidad',
    '5000.00',
    'Socio 1',
    'Diamantes',
    'Lote de prueba',
    'disponible'
);

INSERT INTO lote (nombre, peso_total, cantidad_piedras, tipo_piedra, calidad_piedra, descripcion, precio_estimado, socio, categoria, observaciones, estado)
VALUES (
    'Lote Esmeraldas Finas',
    85.0,
    15,
    'Esmeralda',
    'Alta',
    'Esmeraldas colombianas de excelente calidad',
    '3200.00',
    'Socio 2',
    'Esmeraldas',
    'Lote premium',
    'disponible'
);
*/

-- 4. Verificar que la tabla fue creada correctamente
-- Ejecuta: SELECT * FROM lote;

-- 5. Ver estructura de la tabla
-- Ejecuta: \d lote (en psql)
-- o en DBeaver: clic derecho en tabla → Properties

-- ============================================================
-- Descripción de campos:
-- ============================================================
-- id                : Identificador único (auto-generado)
-- nombre            : Nombre descriptivo del lote
-- peso_total        : Peso total en gramos
-- cantidad_piedras  : Número de unidades/piedras en el lote
-- tipo_piedra       : Tipo (Diamante, Esmeralda, Rubí, Zafiro, etc.)
-- calidad_piedra    : Calidad (Premium, Alta, Media, Regular)
-- fecha_creacion    : Fecha de creación (auto-generada)
-- descripcion       : Descripción detallada
-- precio_estimado   : Precio estimado del lote
-- precio_venta_real : Precio real de venta (si se vendió)
-- socio             : Nombre del socio responsable
-- punto_fisico      : Punto fisico asociado a la venta/registro
-- categoria         : Categoría (debe coincidir con categorías existentes)
-- estado            : Estado (disponible, vendido, etc.)
-- fecha_venta       : Fecha en que fue vendido (NULL si no vendido)
-- vendido           : Boolean indicando si fue vendido
-- observaciones     : Notas adicionales
-- autorizado        : Si fue autorizado por admin (TRUE por defecto)
-- actualizado_en    : Fecha de última actualización
-- actualizado_por   : ID del usuario que actualizó

