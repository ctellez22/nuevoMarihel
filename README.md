# Marihel para macOS

## Qué incluye
- Empaquetado nativo para macOS con `jpackage`
- Runtime de Java embebido dentro de la app `.app`
- Generación de `Marihel.app` y `Marihel-1.0.0.dmg`
- Configuración externa de base de datos en `~/Library/Application Support/Marihel/config.properties`

## Requisitos para construir
- macOS
- JDK 21 con `jpackage`
- Maven 3.9+

## Configuración de base de datos
Crea este archivo:

`~/Library/Application Support/Marihel/config.properties`

Con este contenido:

```properties
db.url=jdbc:mysql://metro.proxy.rlwy.net:38179/railway?sslMode=REQUIRED&serverTimezone=UTC
db.user=root
db.password=MZemUaMJeOJDNanLLCHQseOlfgZQEjjR
```

También puedes copiar la plantilla de `packaging/macos/config.properties.example`.

O instalarla automáticamente con:

```zsh
chmod +x scripts/install-config-macos.sh
./scripts/install-config-macos.sh
```

## Construcción de la app para Mac
Desde la raíz del proyecto:

```zsh
chmod +x scripts/build-macos.sh
./scripts/build-macos.sh
```

El script:
1. compila el proyecto con Maven,
2. detecta el JAR ejecutable,
3. genera un icono `.icns` desde `src/main/resources/logo.png`,
4. empaqueta `Marihel.app`,
5. genera `Marihel-1.0.0.dmg`.

## Salida esperada
Los artefactos se generan en:

- `dist/macos/Marihel.app`
- `dist/macos/Marihel-1.0.0.dmg`
- `dist/macos/config.properties.example`

## Ejecutar en desarrollo usando la configuración de macOS

```zsh
chmod +x scripts/run-local-macos.sh
./scripts/run-local-macos.sh
```

## Notas
- Al abrir una `.app` desde Finder, las variables de entorno como `DB_PASSWORD` normalmente no existen. Por eso la app ahora busca automáticamente `~/Library/Application Support/Marihel/config.properties`.
- Si vas a distribuir la app fuera de tu equipo, probablemente querrás firmarla y notarizarla con Apple para evitar advertencias de Gatekeeper.
- Si el acceso a MySQL sigue fallando, revisa permisos remotos del usuario `admin` y la conectividad hacia la instancia RDS.

