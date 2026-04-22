# 🔧 Solución Inmediata: Configurar ZD230 en tu Mac

## El Problema

Tu Mac no tiene CUPS instalado, por eso falla el comando `lp`. Hay dos soluciones:

---

## ✅ SOLUCIÓN RÁPIDA (Recomendada): Socket TCP

Esta es la forma más directa sin instalar nada adicional.

### Paso 1: Obtener la IP de la impresora ZD230

En tu impresora física:
1. Presiona el botón de menú
2. Busca "Network" o "Configuración"
3. Anota la dirección IP (ej: `192.168.1.100`)

O desde tu Mac:
```bash
# Busca dispositivos Zebra en la red
arp -a | grep -i zebra

# O prueba si la impresora está conectada
ping 192.168.1.XX  # reemplaza XX con el rango
```

### Paso 2: Verificar conectividad

```bash
# Verifica que puedes alcanzar el puerto 9100
nc -zv 192.168.1.100 9100

# Si ves: "Connection succeeded", ¡excelente!
# Si no, verifica que la impresora está encendida y en la misma red
```

### Paso 3: Ejecutar con configuración de socket

```bash
# Opción A: Con propiedad Java
java -Dprinter.ip=192.168.1.100 -Dprinter.port=9100 -jar marihel.jar

# Opción B: Con variables de entorno
export PRINTER_IP=192.168.1.100
export PRINTER_PORT=9100
java -jar marihel.jar

# Opción C: En el script run-local-macos.sh
# Edita el archivo y añade estas líneas:
export PRINTER_IP=192.168.1.100
export PRINTER_PORT=9100
```

---

## 🔄 SOLUCIÓN ALTERNATIVA: Instalar CUPS

Si prefieres usar el comando `lp` tradicional:

### Paso 1: Instalar CUPS

```bash
# Primero verifica que Homebrew está instalado
brew --version

# Si no está:
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Ahora instala CUPS
chmod +x scripts/install-cups-macos.sh
./scripts/install-cups-macos.sh

# O manualmente:
brew install cups
```

### Paso 2: Agregar la impresora a macOS

1. **Abre Sistema → Impresoras y escáneres**
2. Haz clic en el botón **+** (Agregar)
3. Busca y selecciona tu impresora Zebra ZD230
4. Haz clic en **Agregar**

### Paso 3: Verificar que funcionó

```bash
# Debe listar tu impresora
lpstat -p -d

# Ejemplo de salida:
# printer Zebra_Technologies_ZTC_ZD230_ZPL is idle...

# Si necesitas el nombre exacto:
lpstat -p -d | grep -i zebra
```

### Paso 4: Ejecutar la app

```bash
# Si la impresora se detectó automáticamente:
./scripts/run-local-macos.sh

# Si no, especifica el nombre exacto:
java -Dprinter.name="Zebra_Technologies_ZTC_ZD230_ZPL" -jar marihel.jar
```

---

## 🧪 Pruebas de Diagnóstico

### Test 1: ¿Está la impresora en la red?
```bash
ping 192.168.1.100
# Deberías ver respuestas (packets)
```

### Test 2: ¿Está accesible el puerto 9100?
```bash
nc -zv 192.168.1.100 9100
# Deberías ver: "Connection succeeded"
```

### Test 3: ¿Qué impresoras ve macOS?
```bash
lpstat -p -d
# Verifica que tu ZD230 aparece aquí
```

### Test 4: ¿Se creó el archivo ZPL?
```bash
ls -la ~/Desktop/bebeBoste.zpl
# Debe existir este archivo
```

### Test 5: ¿Se puede enviar directamente al puerto?
```bash
# Envía un comando ZPL simple de prueba
echo "^XA^FO50,50^A0N,30,30^FDTest^FS^XZ" | nc 192.168.1.100 9100
# Si funciona, la impresora imprimirá "Test"
```

---

## 📝 Resumen de Comandos

| Tarea | Comando |
|-------|---------|
| Instalar CUPS | `brew install cups` |
| Ver impresoras | `lpstat -p -d` |
| Buscar Zebra en red | `arp -a \| grep -i zebra` |
| Ping a impresora | `ping 192.168.1.100` |
| Verificar puerto 9100 | `nc -zv 192.168.1.100 9100` |
| Ejecutar con socket | `java -Dprinter.ip=192.168.1.100 -jar marihel.jar` |

---

## ⚡ Recomendación Personal

👉 **Usa Socket TCP** (Opción 1)

Razones:
- ✅ No requiere instalar nada
- ✅ Funciona incluso si macOS cambia CUPS
- ✅ Es más rápido y confiable en redes
- ✅ Funciona en Windows, Linux y macOS
- ✅ Ideal para impresoras de red modernas

Solo necesitas:
1. IP de la impresora
2. Una línea: `export PRINTER_IP=192.168.1.100`

---

## ❓ Preguntas Frecuentes

**P: ¿Cuál es la IP de mi ZD230?**
R: Mira el menú de la impresora o busca con `arp -a`

**P: ¿El puerto siempre es 9100?**
R: Sí, es el estándar Zebra ZPL. Si tu configuración es diferente, verifica en el menú de la impresora.

**P: ¿Necesito CUPS?**
R: No, Socket TCP es suficiente. Pero si la impresora está en "Impresoras y escáneres" de macOS, CUPS ya está funcionando.

**P: ¿Qué pasa si están los dos métodos disponibles?**
R: El código intenta primero Socket TCP (más rápido), luego `lp`. El primero que funcione, gana.

---

## 🚀 Siguientes Pasos

1. Encuentra la IP de tu ZD230
2. Verifica conectividad con `nc -zv 192.168.1.X 9100`
3. Ejecuta con: `export PRINTER_IP=192.168.1.X && ./scripts/run-local-macos.sh`
4. ¡Crea una joya y debería imprimirse!

Si aún tienes problemas, revisa los logs de la aplicación: busca las líneas con "✅" o "❌" que indican qué método se intentó.

