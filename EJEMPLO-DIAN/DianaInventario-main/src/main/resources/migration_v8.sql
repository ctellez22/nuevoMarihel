-- Migration V8: modulo de orden de trabajo

CREATE TABLE IF NOT EXISTS orden_trabajo (
    id              BIGSERIAL PRIMARY KEY,
    joya_id         BIGINT       NOT NULL,
    joya_display_id VARCHAR(255),
    joya_nombre     VARCHAR(255),
    joyero          VARCHAR(255) NOT NULL,
    fecha_envio     TIMESTAMP    NOT NULL,
    fecha_entrega   TIMESTAMP    NOT NULL,
    detalle         TEXT,
    estado          VARCHAR(50)  NOT NULL DEFAULT 'pendiente',
    creado_en       TIMESTAMP    NOT NULL DEFAULT NOW(),
    punto_fisico    VARCHAR(255),
    CONSTRAINT fk_orden_trabajo_joya FOREIGN KEY (joya_id) REFERENCES joya (id)
);

CREATE INDEX IF NOT EXISTS idx_orden_trabajo_joya ON orden_trabajo (joya_id);
CREATE INDEX IF NOT EXISTS idx_orden_trabajo_estado ON orden_trabajo (estado);

