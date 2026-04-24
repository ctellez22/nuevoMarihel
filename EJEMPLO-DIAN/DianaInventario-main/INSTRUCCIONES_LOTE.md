# 🗄️ INSTRUCCIONES PARA CREAR LA TABLA LOTE EN POSTGRESQL

## Opción 1: Ejecutar el schema completo (RECOMENDADO)

Si estás iniciando la aplicación por primera vez o quieres recrear todas las tablas:

```bash
# Desde psql o DBeaver, ejecuta:
\i /ruta/a/schema.sql
```

O si usas DBeaver:
1. Abre tu conexión a la BD
2. Clic derecho en la BD → SQL Editor → New SQL Script
3. Copia el contenido de `src/main/resources/schema.sql`
4. Ejecuta (Ctrl+Enter o Cmd+Enter)

---

## Opción 2: Ejecutar solo la tabla LOTE

Si ya tienes la BD creada y solo necesitas agregar la tabla LOTE:

```sql
-- Copia y ejecuta esto en psql o DBeaver:

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
```

---

## Verificar que la tabla se creó

```sql
-- Ver todas las tablas:
\dt

-- Ver estructura de la tabla lote:
\d lote

-- Ver información detallada:
SELECT * FROM information_schema.columns 
WHERE table_name = 'lote';
```

---

## Insertar datos de prueba (opcional)

```sql
INSERT INTO lote (nombre, peso_total, cantidad_piedras, tipo_piedra, calidad_piedra, descripcion, precio_estimado, socio, categoria, observaciones)
VALUES (
    'Lote Diamantes Test',
    150.5,
    25,
    'Diamante',
    'Premium',
    'Lote de prueba para diamantes',
    '5000.00',
    'Socio 1',
    'Diamantes',
    'Este es un lote de prueba'
);
```

---

## Consultas útiles

```sql
-- Ver todos los lotes:
SELECT * FROM lote;

-- Ver lotes disponibles:
SELECT * FROM lote WHERE estado = 'disponible';

-- Ver lotes vendidos:
SELECT * FROM lote WHERE estado = 'vendido';

-- Ver lotes no autorizados (pendientes):
SELECT * FROM lote WHERE autorizado = FALSE;

-- Contar lotes por socio:
SELECT socio, COUNT(*) as cantidad FROM lote GROUP BY socio;

-- Lotes con peso total > 100 gramos:
SELECT nombre, peso_total, cantidad_piedras FROM lote WHERE peso_total > 100;
```

---

## Notas importantes

- La tabla se crea con `IF NOT EXISTS`, así no hay problema si ya existe
- El campo `fecha_creacion` se auto-completa con la fecha/hora actual
- El campo `estado` por defecto es 'disponible'
- El campo `autorizado` por defecto es TRUE
- El campo `vendido` por defecto es FALSE
- La columna `actualizado_por` hace referencia a la tabla `app_usuario`

---

## Si algo falla

### Error: "relation 'lote' already exists"
Significa que la tabla ya existe. Puedes:
- Dejarla como está (no es un problema)
- O ejecutar: `DROP TABLE IF EXISTS lote;` y luego crear de nuevo

### Error: "constraint 'fk_lote_actualizado_por' already exists"
Ejecuta: `DROP CONSTRAINT IF EXISTS fk_lote_actualizado_por;` en la tabla lote

### Error: "table 'app_usuario' does not exist"
Significa que la tabla de usuarios no está creada. Ejecuta primero el `schema.sql` completo.

---

## Para usar en la aplicación

Una vez que la tabla existe en la BD:

1. Abre la aplicación
2. Ve a "Cargar Datos"
3. Verás dos radio buttons: "Crear Joya" y "Crear Lote"
4. Selecciona "Crear Lote"
5. Se abrirá una interfaz con formulario para crear un lote
6. Completa los campos requeridos
7. Haz clic en "Guardar"
8. Si eres admin: se guarda directamente
9. Si eres no-admin: se registra como "Pendiente de aprobación"

