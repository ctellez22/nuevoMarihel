#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TARGET_DIR="$HOME/Library/Application Support/Marihel"
TARGET_FILE="$TARGET_DIR/config.properties"
SOURCE_FILE="$ROOT_DIR/packaging/macos/config.properties.example"

mkdir -p "$TARGET_DIR"

if [[ -f "$TARGET_FILE" ]]; then
  echo "Ya existe: $TARGET_FILE"
  echo "No se sobrescribió. Edítalo manualmente si quieres cambiar credenciales."
  exit 0
fi

cp "$SOURCE_FILE" "$TARGET_FILE"
echo "Configuración creada en: $TARGET_FILE"
echo "Edita db.url, db.user y db.password según tu ambiente antes de abrir la app."

