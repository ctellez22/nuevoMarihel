# Guía de Configuración de la Impresora Zebra ZD230

## 1. En macOS

### Verificar el nombre de la impresora instalada

```bash
# Listar todas las impresoras
lpstat -p -d

# Ejemplo de salida:
# printer Zebra_Technologies_ZTC_ZD230_ZPL is idle...
```

### Configurar el nombre de la impresora

Si tu impresora tiene un nombre diferente, puedes especificarlo de dos formas:

#### Opción A: Variable de sistema
```bash
export PRINTER_NAME="Tu_Nombre_de_Impresora"
java -Dprinter.name="Tu_Nombre_de_Impresora" -jar marihel.jar
```

#### Opción B: En el script de ejecución
Edita `scripts/run-local-macos.sh` y añade:
```bash
export JAVA_OPTS="-Dprinter.name=Zebra_Technologies_ZTC_ZD230_ZPL"
```

### Verificar conectividad

```bash
# Hacer ping a la impresora
ping 192.168.1.100  # Cambia por tu IP de impresora

# Probar conexión al puerto 9100
nc -zv 192.168.1.100 9100
```

---

## 2. En Windows

### Verificar el nombre de la impresora

1. **Opción 1: Desde PowerShell**
```powershell
Get-Printer -Name "Zebra*"
```

2. **Opción 2: Desde GUI**
   - Configuración > Dispositivos > Impresoras y escáneres
   - Busca tu impresora Zebra
   - Anota el nombre exacto

### Configurar RawPrint

1. Descarga RawPrint desde Zebra:
   - https://www.zebra.com/us/en/support-portal/knowledge-base.html

2. Coloca `RawPrint.exe` en:
   ```
   C:\Users\ASUS\OneDrive\Escritorio\RawPrint.exe
   ```
   (o actualiza la ruta en `Impresora.java`)

3. Verifica que la impresora está conectada:
```cmd
netstat -an | findstr 9100
```

### Configurar el nombre de impresora en Windows

```cmd
# Ejecutar la aplicación con el nombre de impresora específico
java -Dprinter.name="ZDesigner ZD230" -jar marihel.jar
```

---

## 3. Configuración por Red (TCP Socket)

Si prefieres enviar directamente por red en lugar de usar el comando `lp`:

### En macOS o Windows
```bash
# Terminal macOS
java -Dprinter.ip=192.168.1.100 -Dprinter.port=9100 -jar marihel.jar

# cmd Windows
java -Dprinter.ip=192.168.1.100 -Dprinter.port=9100 -jar marihel.jar
```

### O con variables de entorno
```bash
export PRINTER_IP=192.168.1.100
export PRINTER_PORT=9100
java -jar marihel.jar
```

---

## 4. Solución de Problemas

### Error: "Comando 'lp' no disponible"

**En macOS:**
```bash
# Verifica que lp está instalado
which lp

# Si no está, instálalo con Homebrew
brew install cups
```

### Error: "Impresora no encontrada"

**Paso 1:** Verifica el nombre exacto
```bash
lpstat -p -d
```

**Paso 2:** Usa ese nombre exacto:
```bash
java -Dprinter.name="TU_NOMBRE_EXACTO" -jar marihel.jar
```

### Error: "No se puede conectar al puerto 9100"

**Solución:**
1. Verifica que la impresora tiene IP estática
2. Prueba ping: `ping 192.168.1.100`
3. Prueba telnet: `telnet 192.168.1.100 9100`
4. Reinicia la impresora

### Las etiquetas no se imprimen correctamente

1. **Verifica el formato ZPL** - Asegúrate de que es válido
2. **Revisa los logs** - Mira qué método se está usando
3. **Prueba con un archivo ZPL de prueba:**
   ```
   ^XA
   ^FO50,50^A0N,30,30^FDHola Mundo^FS
   ^XZ
   ```

---

## 5. Información Técnica ZD230

- **Puerto TCP por defecto:** 9100
- **Idioma:** ZPL (Zebra Programming Language)
- **Resolución:** 203 DPI o 300 DPI (configurable)
- **Ancho máximo:** 104 mm (formato estándar)
- **Charset:** UTF-8 o ASCII

### Especificaciones de Etiqueta

El proyecto genera etiquetas con formato:
- Ancho: `^PW984` (984 dots a 203 DPI ≈ 104 mm)
- Alto: `^LL102` (102 dots a 203 DPI ≈ 12.7 mm)

---

## 6. Comandos Útiles para Depuración

### macOS
```bash
# Ver todas las opciones de impresión
lp -h

# Imprimir un archivo directamente
lp -d "Zebra_Technologies_ZTC_ZD230_ZPL" -o raw test.zpl

# Ver estado de la impresora
lpstat -t

# Ver cola de impresión
lpq
```

### Windows
```cmd
# Ver impresoras instaladas
Get-Printer (PowerShell)

# Verificar puerto
netstat -an | findstr :9100

# Probar RawPrint
RawPrint /f "ruta\archivo.zpl" /pr "Nombre Impresora"
```

---

## 7. Verificación Final

Para confirmar que todo funciona:

1. **En macOS:**
   ```bash
   ./scripts/run-local-macos.sh
   # Crear una joya y verificar que se imprime
   ```

2. **En Windows:**
   - Ejecutar la aplicación
   - Crear una joya
   - Verificar que aparece en la cola de impresión

3. **Validación:**
   - Revisa los logs en la consola
   - Busca el mensaje: "Etiqueta enviada..."
   - La impresora debe emitir una etiqueta

