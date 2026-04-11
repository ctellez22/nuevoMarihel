#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CONFIG_FILE_DEFAULT="$HOME/Library/Application Support/Marihel/config.properties"

if [[ -f "$CONFIG_FILE_DEFAULT" ]]; then
  exec mvn -f "$ROOT_DIR/pom.xml" exec:java -Dexec.mainClass=org.example.Main -Dapp.config="$CONFIG_FILE_DEFAULT"
fi

cat <<EOF
No se encontró configuración en:
$CONFIG_FILE_DEFAULT

Crea ese archivo con el formato:
  db.url=jdbc:mysql://host:3306/base?sslMode=REQUIRED&serverTimezone=UTC
  db.user=admin
  db.password=adminadmin
EOF
exit 1

