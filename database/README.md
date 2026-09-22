# Base de datos

Carpeta de scripts SQL del proyecto.

## Instalación (archivo único)

El script **`maps_conect.sql`** es el archivo canónico de instalación. Incluye todo:
creación de la base, esquema (26 tablas + triggers), catálogos (carreras, materias,
plan de estudios, empresas) y datos demo (foro, recursos, círculos, reseñas,
seguimientos, conversaciones y tips).

```bash
mysql -u root -p < maps_conect.sql
```

En Windows usar redirección de `cmd` (la tubería de PowerShell corrompe UTF‑8):

```cmd
cmd /c "mysql -u root -p --default-character-set=utf8mb4 < maps_conect.sql"
```

Usuario demo logueable: `al07080560@tecmilenio.mx` / `DemoMaps2026!`

## Otros scripts (referencia)

- `schema.sql` — estructura original (desactualizado; ver `maps_conect.sql`).
- `seed-demo.sql` — datos demo del foro, recursos, círculos y reseñas.
- `seed-follows.sql` — tabla `seguimientos` + compañeros demo.
- `seed-mensajes-tips.sql` — conversaciones, mensajes y tips académicos.
- `clean-demo.sql` — limpieza de los datos demo (no es parte de la instalación).
