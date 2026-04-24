-- ==============================================================
-- CONSULTAS ÚTILES PARA LA TABLA LOTE
-- Ejecuta estos comandos en psql o DBeaver para verificar y
-- consultar los lotes guardados
-- ==============================================================

-- 1. VER ESTRUCTURA DE LA TABLA
-- ============================================================

-- Ver la definición completa de la tabla lote:
\d lote

-- Ver información de columnas:
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'lote'
ORDER BY ordinal_position;

-- Ver índices de la tabla:
SELECT * FROM pg_indexes WHERE tablename = 'lote';

-- Ver constrains (claves foráneas, etc):
SELECT
    constraint_name,
    constraint_type,
    table_name
FROM information_schema.table_constraints
WHERE table_name = 'lote';


-- 2. CONSULTAS BÁSICAS
-- ============================================================

-- Ver TODOS los lotes:
SELECT * FROM lote ORDER BY fecha_creacion DESC;

-- Ver resumen de lotes:
SELECT
    id,
    nombre,
    tipo_piedra,
    peso_total,
    cantidad_piedras,
    estado,
    fecha_creacion
FROM lote
ORDER BY fecha_creacion DESC;

-- Contar total de lotes:
SELECT COUNT(*) as total_lotes FROM lote;

-- Ver lotes recientes (últimos 7 días):
SELECT
    id, nombre, tipo_piedra, estado, fecha_creacion
FROM lote
WHERE fecha_creacion >= NOW() - INTERVAL '7 days'
ORDER BY fecha_creacion DESC;


-- 3. FILTRAR POR ESTADO
-- ============================================================

-- Ver lotes DISPONIBLES:
SELECT id, nombre, peso_total, tipo_piedra, precio_estimado, socio
FROM lote
WHERE estado = 'disponible'
ORDER BY fecha_creacion DESC;

-- Ver lotes VENDIDOS:
SELECT
    id, nombre, precio_estimado, precio_venta_real,
    fecha_venta, socio
FROM lote
WHERE estado = 'vendido'
ORDER BY fecha_venta DESC;

-- Ver lotes NO AUTORIZADOS (pendientes):
SELECT
    id, nombre, tipo_piedra, socio,
    fecha_creacion, autorizado
FROM lote
WHERE autorizado = FALSE
ORDER BY fecha_creacion;

-- Contar lotes disponibles:
SELECT COUNT(*) as lotes_disponibles FROM lote WHERE estado = 'disponible';

-- Contar lotes vendidos:
SELECT COUNT(*) as lotes_vendidos FROM lote WHERE estado = 'vendido';

-- Contar lotes pendientes de autorización:
SELECT COUNT(*) as lotes_pendientes FROM lote WHERE autorizado = FALSE;


-- 4. FILTRAR POR TIPO DE PIEDRA
-- ============================================================

-- Ver todos los tipos de piedra disponibles:
SELECT DISTINCT tipo_piedra FROM lote ORDER BY tipo_piedra;

-- Ver lotes de DIAMANTES:
SELECT id, nombre, peso_total, cantidad_piedras, precio_estimado
FROM lote
WHERE tipo_piedra = 'Diamante'
ORDER BY peso_total DESC;

-- Ver lotes de ESMERALDAS:
SELECT id, nombre, peso_total, cantidad_piedras, precio_estimado
FROM lote
WHERE tipo_piedra = 'Esmeralda'
ORDER BY fecha_creacion DESC;

-- Ver lotes por tipo de piedra (resumen):
SELECT
    tipo_piedra,
    COUNT(*) as cantidad,
    SUM(peso_total) as peso_total,
    AVG(cantidad_piedras) as promedio_piedras
FROM lote
GROUP BY tipo_piedra
ORDER BY cantidad DESC;


-- 5. FILTRAR POR SOCIO
-- ============================================================

-- Ver lotes por SOCIO:
SELECT
    socio,
    COUNT(*) as cantidad_lotes,
    SUM(peso_total) as peso_total,
    COUNT(CASE WHEN estado = 'disponible' THEN 1 END) as disponibles,
    COUNT(CASE WHEN estado = 'vendido' THEN 1 END) as vendidos
FROM lote
GROUP BY socio
ORDER BY cantidad_lotes DESC;

-- Ver todos los lotes de un socio específico:
SELECT id, nombre, tipo_piedra, peso_total, estado, fecha_creacion
FROM lote
WHERE socio = 'Socio 1'
ORDER BY fecha_creacion DESC;


-- 6. ANÁLISIS DE PRECIOS
-- ============================================================

-- Ver resumen de precios:
SELECT
    id, nombre,
    precio_estimado::NUMERIC as precio_est,
    precio_venta_real::NUMERIC as precio_venta,
    (precio_venta_real::NUMERIC - precio_estimado::NUMERIC) as diferencia,
    estado
FROM lote
WHERE precio_venta_real IS NOT NULL
ORDER BY fecha_venta DESC;

-- Ver ganancia/pérdida de lotes vendidos:
SELECT
    id, nombre,
    TO_NUMBER(precio_estimado, '999G999D99') as precio_est,
    TO_NUMBER(precio_venta_real, '999G999D99') as precio_venta,
    TO_NUMBER(precio_venta_real, '999G999D99') - TO_NUMBER(precio_estimado, '999G999D99') as ganancia
FROM lote
WHERE precio_venta_real IS NOT NULL
ORDER BY ganancia DESC;


-- 7. BÚSQUEDA POR NOMBRE O DESCRIPCIÓN
-- ============================================================

-- Buscar por nombre (parcial):
SELECT id, nombre, tipo_piedra, estado, fecha_creacion
FROM lote
WHERE nombre ILIKE '%diamante%'
ORDER BY fecha_creacion DESC;

-- Buscar en descripción:
SELECT id, nombre, descripcion, tipo_piedra
FROM lote
WHERE descripcion ILIKE '%premium%'
ORDER BY fecha_creacion DESC;


-- 8. ANÁLISIS POR FECHAS
-- ============================================================

-- Lotes creados HOY:
SELECT id, nombre, tipo_piedra, peso_total, socio
FROM lote
WHERE DATE(fecha_creacion) = CURRENT_DATE
ORDER BY fecha_creacion DESC;

-- Lotes creados en el ÚLTIMO MES:
SELECT
    id, nombre, tipo_piedra, peso_total,
    DATE(fecha_creacion) as fecha
FROM lote
WHERE fecha_creacion >= NOW() - INTERVAL '1 month'
ORDER BY fecha_creacion DESC;

-- Lotes vendidos en el ÚLTIMO MES:
SELECT
    id, nombre, precio_venta_real,
    DATE(fecha_venta) as fecha_venta
FROM lote
WHERE estado = 'vendido'
  AND fecha_venta >= NOW() - INTERVAL '1 month'
ORDER BY fecha_venta DESC;

-- Lotes por mes (histograma):
SELECT
    DATE_TRUNC('month', fecha_creacion)::DATE as mes,
    COUNT(*) as cantidad_creados,
    COUNT(CASE WHEN estado = 'vendido' THEN 1 END) as vendidos
FROM lote
GROUP BY DATE_TRUNC('month', fecha_creacion)
ORDER BY mes DESC;


-- 9. ESTADÍSTICAS
-- ============================================================

-- Estadísticas generales:
SELECT
    COUNT(*) as total_lotes,
    COUNT(CASE WHEN estado = 'disponible' THEN 1 END) as disponibles,
    COUNT(CASE WHEN estado = 'vendido' THEN 1 END) as vendidos,
    COUNT(CASE WHEN autorizado = FALSE THEN 1 END) as pendientes_autorización,
    ROUND(SUM(peso_total), 2) as peso_total_gramos,
    ROUND(AVG(peso_total), 2) as peso_promedio,
    MIN(fecha_creacion) as primer_lote,
    MAX(fecha_creacion) as ultimo_lote
FROM lote;

-- Estadísticas por calidad:
SELECT
    calidad_piedra,
    COUNT(*) as cantidad,
    ROUND(AVG(peso_total), 2) as peso_promedio,
    ROUND(AVG(cantidad_piedras::NUMERIC), 2) as piedras_promedio
FROM lote
WHERE calidad_piedra IS NOT NULL
GROUP BY calidad_piedra
ORDER BY cantidad DESC;


-- 10. ACTUALIZACIÓN Y MANTENIMIENTO
-- ============================================================

-- MARCAR UN LOTE COMO VENDIDO:
UPDATE lote
SET
    estado = 'vendido',
    vendido = TRUE,
    fecha_venta = NOW(),
    precio_venta_real = '5500.00'  -- Cambiar por el precio real
WHERE id = 1;  -- Cambiar ID según corresponda

-- ACTUALIZAR OBSERVACIONES:
UPDATE lote
SET observaciones = 'Observación actualizada'
WHERE id = 1;

-- CAMBIAR ESTADO AUTORIZACIÓN:
UPDATE lote
SET autorizado = TRUE
WHERE id = 1;

-- ELIMINAR UN LOTE (¡CUIDADO!):
-- DELETE FROM lote WHERE id = 1;


-- 11. AUDITORÍA
-- ============================================================

-- Ver lotes actualizados recientemente:
SELECT
    id, nombre,
    fecha_creacion,
    actualizado_en,
    CASE WHEN actualizado_en IS NOT NULL
        THEN 'Actualizado'
        ELSE 'Original'
    END as estado_registro
FROM lote
WHERE actualizado_en IS NOT NULL
ORDER BY actualizado_en DESC;

-- Ver cambios pendientes de aprobación:
SELECT
    cp.id,
    cp.entidad,
    cp.entidad_id,
    cp.operacion,
    cp.estado,
    cp.solicitado_en,
    au.username as solicitado_por
FROM cambio_pendiente cp
LEFT JOIN app_usuario au ON cp.solicitado_por = au.id
WHERE cp.entidad = 'lote'
ORDER BY cp.solicitado_en DESC;


-- 12. EXPORTAR DATOS
-- ============================================================

-- Exportar a CSV (ejecutar en psql):
-- \COPY (SELECT * FROM lote ORDER BY fecha_creacion DESC) TO 'lotes_export.csv' WITH CSV HEADER;

-- Ver datos con formato "amigable":
SELECT
    id as "ID",
    nombre as "Nombre",
    peso_total as "Peso (g)",
    cantidad_piedras as "Cantidad",
    tipo_piedra as "Tipo",
    calidad_piedra as "Calidad",
    estado as "Estado",
    TO_CHAR(fecha_creacion, 'DD/MM/YYYY HH:MM') as "Fecha Creación",
    socio as "Socio"
FROM lote
ORDER BY fecha_creacion DESC;

-- ==============================================================
-- FIN DE CONSULTAS ÚTILES
-- ==============================================================

