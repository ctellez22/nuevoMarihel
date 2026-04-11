#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist/macos"
ICON_DIR="$ROOT_DIR/packaging/macos"
ICON_FILE="$ICON_DIR/Marihel.icns"
ICONSET_DIR="$ICON_DIR/Marihel.iconset"
APP_NAME="Marihel"
APP_VERSION="1.0.0"
MAIN_CLASS="org.example.Main"
BUNDLE_ID="com.marihel.inventario"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Error: se requiere '$1' para continuar." >&2
    exit 1
  fi
}

create_icon() {
  local source_png="$ROOT_DIR/src/main/resources/logo.png"

  if [[ -f "$ICON_FILE" ]]; then
    return
  fi

  if [[ ! -f "$source_png" ]]; then
    echo "Aviso: no se encontró $source_png; jpackage usará el icono por defecto."
    return
  fi

  require_command sips
  require_command iconutil

  mkdir -p "$ICON_DIR"
  rm -rf "$ICONSET_DIR"
  mkdir -p "$ICONSET_DIR"

  local sizes=(16 32 64 128 256 512)
  for size in "${sizes[@]}"; do
    sips -z "$size" "$size" "$source_png" --out "$ICONSET_DIR/icon_${size}x${size}.png" >/dev/null
    local retina=$((size * 2))
    sips -z "$retina" "$retina" "$source_png" --out "$ICONSET_DIR/icon_${size}x${size}@2x.png" >/dev/null
  done

  iconutil -c icns "$ICONSET_DIR" -o "$ICON_FILE"
}

build_jar() {
  require_command mvn
  (cd "$ROOT_DIR" && mvn -DskipTests package)
}

find_main_jar() {
  find "$ROOT_DIR/target" -maxdepth 1 -type f -name '*.jar' \
    ! -name 'original-*.jar' \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' | head -n 1
}

package_app() {
  require_command jpackage

  local main_jar
  main_jar="$(find_main_jar)"

  if [[ -z "$main_jar" ]]; then
    echo "Error: no se encontró el JAR principal en target/." >&2
    exit 1
  fi

  mkdir -p "$DIST_DIR"
  rm -rf "$DIST_DIR/$APP_NAME.app" "$DIST_DIR/$APP_NAME" "$DIST_DIR/$APP_NAME-$APP_VERSION.dmg"

  local jpackage_args=(
    --name "$APP_NAME"
    --input "$ROOT_DIR/target"
    --main-jar "$(basename "$main_jar")"
    --main-class "$MAIN_CLASS"
    --dest "$DIST_DIR"
    --type app-image
    --app-version "$APP_VERSION"
    --vendor "Marihel"
    --copyright "© Marihel"
    --description "Inventario Marihel"
    --mac-package-name "$APP_NAME"
    --mac-package-identifier "$BUNDLE_ID"
    --java-options "-Dapple.awt.application.name=$APP_NAME"
    --java-options "-Dfile.encoding=UTF-8"
  )

  if [[ -f "$ICON_FILE" ]]; then
    jpackage_args+=(--icon "$ICON_FILE")
  fi

  jpackage "${jpackage_args[@]}"

  local dmg_args=("${jpackage_args[@]}")
  dmg_args=("${dmg_args[@]/--type/}")
  # Reconstruir args para evitar residuos de app-image.
  dmg_args=(
    --name "$APP_NAME"
    --input "$ROOT_DIR/target"
    --main-jar "$(basename "$main_jar")"
    --main-class "$MAIN_CLASS"
    --dest "$DIST_DIR"
    --type dmg
    --app-version "$APP_VERSION"
    --vendor "Marihel"
    --copyright "© Marihel"
    --description "Inventario Marihel"
    --mac-package-name "$APP_NAME"
    --mac-package-identifier "$BUNDLE_ID"
    --java-options "-Dapple.awt.application.name=$APP_NAME"
    --java-options "-Dfile.encoding=UTF-8"
  )

  if [[ -f "$ICON_FILE" ]]; then
    dmg_args+=(--icon "$ICON_FILE")
  fi

  jpackage "${dmg_args[@]}"

  cp "$ROOT_DIR/packaging/macos/config.properties.example" "$DIST_DIR/config.properties.example"

  cat <<EOF
Empaquetado completado.

Artefactos generados:
- $DIST_DIR/$APP_NAME.app
- $DIST_DIR/$APP_NAME-$APP_VERSION.dmg
- $DIST_DIR/config.properties.example

Configura la base de datos creando:
~/Library/Application Support/$APP_NAME/config.properties
EOF
}

create_icon
build_jar
package_app

