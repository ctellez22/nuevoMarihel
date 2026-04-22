#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG_FILE_DEFAULT="$HOME/Library/Application Support/Marihel/config.properties"

# Configuración de impresora (opcional)
# Descomenta y modifica según tu impresora:
# export PRINTER_IP=192.168.1.100
# export PRINTER_PORT=9100

if [[ -f "$CONFIG_FILE_DEFAULT" ]]; then
  # Paso todas las variables de entorno a Maven
  exec mvn -f "$ROOT_DIR/pom.xml" exec:java \
    -Dexec.mainClass=org.example.Main \
    -Dapp.config="$CONFIG_FILE_DEFAULT"
fi

cat <<EOF
No se encontró configuración en:
$CONFIG_FILE_DEFAULT

Crea ese archivo con el formato:
  db.url=jdbc:mysql://host:3306/base?sslMode=REQUIRED&serverTimezone=UTC
  db.user=admin
  db.password=adminadmin

CONFIGURACIÓN OPCIONAL DE IMPRESORA:
Si deseas usar impresión por socket TCP, edita este script y descomenta:
  export PRINTER_IP=192.168.1.XXX
  export PRINTER_PORT=9100
(reemplaza XXX con la IP de tu impresora ZD230)
EOF
exit 1

