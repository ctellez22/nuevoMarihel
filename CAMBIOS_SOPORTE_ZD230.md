# Cambios para Soportar Impresora ZD230

## Resumen de Modificaciones

Se ha actualizado el código para proporcionar soporte completo para la impresora Zebra ZD230, manteniendo compatibilidad con el modelo anterior ZD421.

## Cambios Realizados

### 1. **Clase `Impresora.java` - `imprimirEnWindows()`**
- **Cambio**: Se actualizó el nombre predeterminado de impresora de `ZDesigner ZD421-300dpi ZPL` a `ZDesigner ZD230`
- **Beneficio**: Ahora detecta automáticamente la ZD230 en Windows como impresora predeterminada
- **Configuración**: Sigue siendo configurable mediante la propiedad `printer.name`

### 2. **Clase `Impresora.java` - `imprimirEnMac()`**
- **Mejora**: Se agregó detección automática de impresoras Zebra disponibles
- **Nuevo Método**: `detectarImpresoraMac()` que busca:
  - `Zebra_Technologies_ZTC_ZD230_ZPL` (ZD230 primaria)
  - `ZD230`
  - `Zebra_Technologies_ZTC_ZD421_203dpi_ZPL` (ZD421 alternativa)
  - `ZD421`
  - `Zebra_ZD230`
  - `Zebra_ZD421`
- **Beneficio**: Detecta automáticamente qué impresora está disponible en el sistema macOS

### 3. **Flujo de Impresión en macOS**
El sistema ahora intenta 3 métodos en orden de preferencia:

1. **Método 1**: `/usr/bin/lp` con opción `-o raw` (más confiable)
2. **Método 2**: `lp` en PATH (si la ruta absoluta no funciona)
3. **Método 3**: Envío directo por socket TCP al puerto 9100 (fallback)

Cada método emite logs detallados si falla, permitiendo depuración fácil.

### 4. **Mejora de Registros (Logs)**
- Se eliminaron los `printStackTrace()` que no eran robustos
- Se reemplazaron con mensajes de error más detallados
- Mejor seguimiento de qué método de impresión funciona

## Configuración Recomendada

### En macOS
Para obtener el nombre exacto de tu impresora ZD230:
```bash
lpstat -p -d
```

Luego, puedes establecer el nombre en tiempo de ejecución:
```bash
java -Dprinter.name="TU_NOMBRE_EXACTO" -jar marihel.jar
```

### En Windows
Para obtener el nombre exacto de tu impresora ZD230:
1. Abre Configuración > Dispositivos > Impresoras y escáneres
2. Busca tu impresora Zebra
3. Establece la propiedad:
```
-Dprinter.name="NombreExacto"
```

## Compatibilidad

- ✅ Zebra ZD230 (nueva - primaria)
- ✅ Zebra ZD421 (existente - soporte continuo)
- ✅ Windows con RawPrint
- ✅ macOS con `lp` command
- ✅ Impresión remota por socket TCP

## Pruebas Recomendadas

1. **Test en macOS**:
   ```bash
   # Verificar que la impresora sea detectada
   lpstat -p
   
   # Ejecutar app y verificar logs de detección
   ./scripts/run-local-macos.sh
   ```

2. **Test en Windows**:
   - Verificar que RawPrint.exe está disponible
   - Confirmar que el nombre de la impresora en Windows es correcto

3. **Test por Socket** (si es necesario):
   - Establecer IP y puerto de impresora
   - Verificar conectividad de red

## Notas Importantes

- La ZD230 requiere comandos ZPL estándar (el proyecto ya los genera correctamente)
- No se modificó `PrinterUtils.java` ya que ya contenía la implementación necesaria
- La compatibilidad backward con ZD421 se mantiene completamente
- El sistema es robusto y intentará múltiples métodos antes de fallar

