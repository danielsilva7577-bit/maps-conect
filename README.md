# MAPS Connect

**Plataforma Académica y de Mentoría** para Universidad Tecmilenio que conecta estudiantes, docentes y egresados.

## 🎯 ¿Qué es?

MAPS Connect es una red colaborativa que permite:
- 💬 Foro de dudas académicas
- 💡 Compartir tips de estudio
- 📚 Repositorio de apuntes
- 👥 Círculos de estudio
- 💌 Mensajería privada
- 🏢 Reseñas de empresas (Semestre Empresarial)
- 🎓 Gestión del modelo académico MAPS

## 🏗️ Tecnología

| Capa | Tecnología |
|------|-----------|
| **Backend** | Java 21, Spring Boot 3.3.0 |
| **Frontend** | HTML5, CSS3, JavaScript Vanilla |
| **BD** | MySQL 8.0 (22 entidades, 3FN) |

## 🚀 Quick Start

### Base de datos (MySQL 8)

Hay **un solo archivo** de instalación: `database/maps_conect.sql`, que crea la base,
el esquema completo (tablas + triggers) y carga los catálogos y los datos demo:

```bash
mysql -u root -p < database/maps_conect.sql
```

> En Windows no se debe usar la tubería de PowerShell (corrompe UTF‑8); usar redirección
> de `cmd`:
> ```cmd
> cmd /c "mysql -u root -p --default-character-set=utf8mb4 < database/maps_conect.sql"
> ```

El usuario demo logueable es `al07080560@tecmilenio.mx` / `DemoMaps2026!`.

Los demás scripts (`schema.sql`, `seed-*.sql`, `clean-demo.sql`) se conservan como
referencia, pero no es necesario ejecutarlos: `maps_conect.sql` ya los incluye.

> Las credenciales de conexión por defecto están en `src/main/resources/application-dev.yml`.

### Backend

Antes de lanzar, define las variables de entorno (en `.env` o como env vars del sistema):

```bash
export JWT_SECRET="tu-clave-secreta-super-segura-min-64-caracteres"   # Requerida en prod
export CORS_ORIGINS="https://app.mapsconect.com"                        # Orígenes CORS permitidos (coma-separados)
export DB_USER="root"                          # Usuario MySQL
export DB_PASSWORD="tu-password"               # Password MySQL
export DB_HOST="localhost"                      # Host MySQL (prod)
export DB_PORT="3306"                           # Puerto MySQL (prod)
export DB_NAME="maps_conect"                   # Nombre de la base (prod)
```

| Variable | Perfil | Default | Descripción |
|---|---|---|---|
| `JWT_SECRET` | dev/prod | *(fallback dev)* | Clave secreta para firmar tokens JWT. **Obligatoria en prod.** |
| `JWT_EXPIRATION` | dev/prod | `604800000` (7d dev / 1d prod) | Tiempo de expiración del token en ms |
| `CORS_ORIGINS` | dev/prod | `localhost:*` (dev) | Orígenes permitidos para CORS, separados por coma |
| `DB_USER` / `DB_PASSWORD` | dev/prod | `root` / vacío | Credenciales MySQL |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | prod | — | Configuración MySQL en producción |

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
# API en: http://localhost:8080/api
```
O desde IntelliJ IDEA: ejecutar la configuración **MapsConectApplication** (H2 no; usa MySQL, perfil `dev`).

### Frontend
El backend servirá la aplicación (páginas estáticas + API en el mismo puerto):
- Aplicación: `http://localhost:8080/api/`
- Health: `http://localhost:8080/api/health`

> `frontend/` también puede abrirse de forma independiente en VS Code, pero lo oficial es
> servirlo desde Spring (la URL de la API se resuelve sola desde `window.location.origin`).

## 📋 Requisitos

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Usuarios demo (entre otros): `al07080560@tecmilenio.mx` / `DemoMaps2026!` (estudiante),
  `al07080562@tecmilenio.mx` / `DemoMaps2026!` (estudiante de semestre 6, para Semestre Empresarial),
  `al07080561@tecmilenio.mx` / `DemoMaps2026!` (docente)

## 🧪 Páginas nuevas para verificación visual

Abrir `http://localhost:8080/api/pages/inicio.html`, iniciar sesión (Ctrl+F5 para tomar las
versiones `?v=` del cache-busting) y revisar:

| Página | URL | Qué verificar |
|--------|-----|---------------|
| Inicio | `inicio.html` | Con login de docente: banner "Bienvenido, Docente" y sin quick-question |
| Semestre Empresarial | `empresarial.html` | Filtro por sector, chip "Sector:" y crear, editar o eliminar tu propia reseña (login sem 6) |
| Mi Ruta MAPS | `certificados.html` | Ruta y catálogo con semestres/materias, botón "Añadir a mi ruta" (login sem 6) y ruta llena (login sem 3) |

## 🔐 Autenticación

- **JWT Tokens** (7 días en dev, 24 horas en prod; configurable con `JWT_EXPIRATION`)
- Email institucional: `@tecmilenio.mx`
- Contraseña segura: 8+ caracteres, mayúscula, número

## 📂 Estructura

```
maps-conect/
├── src/main/java/...          # Backend Java/Spring
├── frontend/                   # Frontend HTML/CSS/JS
│   ├── index.html
│   ├── css/
│   ├── js/
│   └── pages/
└── pom.xml
```

## 📚 Módulos

| Módulo | Descripción |
|--------|------------|
| 1️⃣ Gestión MAPS | Carreras, certificados, materias |
| 2️⃣ Usuarios | Login, perfiles, roles |
| 3️⃣ Foro | Dudas y respuestas académicas |
| 4️⃣ Tips | Consejos de estudio con votación |
| 5️⃣ Recursos | Apuntes y material de estudio |
| 6️⃣ Mensajes | Chat 1 a 1 estudiante-profesor |
| 7️⃣ Empresarial | Reseñas y experiencias vinculación |
| 8️⃣ Círculos | Grupos de estudio colaborativos |

## 🔗 Enlaces

- 📖 Backend: `http://localhost:8080/api`
- 🌐 Frontend: `http://localhost:5500`
- ✅ Health: `http://localhost:8080/api/health`

## 👨‍💻 Contacto

**Universidad Tecmilenio** - Proyecto MAPS Connect  
[GitHub Repository](https://github.com/danielsilva7577-bit/maps-conect)

---

**v1.0.0** | Agosto 2024 | En Desarrollo
