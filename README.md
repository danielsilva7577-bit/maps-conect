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
| **BD** | MySQL 8.0 (27 entidades, 3FN) |

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
| `STORAGE_TYPE` | dev/prod | `local` | Tipo de almacenamiento: `local` (disco en uploads/) o `s3` (AWS S3 / MinIO / Cloudflare R2) |
| `STORAGE_S3_ENDPOINT` | prod | `https://s3.amazonaws.com` | Endpoint del proveedor S3 |
| `STORAGE_S3_BUCKET` | prod | `maps-conect-storage` | Nombre del bucket para archivos y adjuntos |
| `STORAGE_S3_REGION` | prod | `us-east-1` | Región AWS / S3 |
| `STORAGE_S3_ACCESS_KEY` / `SECRET_KEY` | prod | — | Credenciales IAM para firma SigV4 |
| `REDIS_ENABLED` | prod | `false` | Activa el bus distribuido Redis Pub/Sub para SSE en clusters multi-nodo |
| `REDIS_HOST` / `REDIS_PORT` | prod | `localhost` / `6379` | Host y puerto del cluster/instancia Redis |
| `REDIS_PASSWORD` | prod | — | Contraseña de autenticación para Redis |

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
- Usuarios demo (contraseña universal: `DemoMaps2026!`):

| Carrera / Rol | Correo Demo | Rol | Semestre | Contraseña |
|---|---|---|---|---|
| **Software (ISSC)** | `al07080560@tecmilenio.mx` *(o `al.software@tecmilenio.mx`)* | Estudiante | Semestre 3 / 5 | `DemoMaps2026!` |
| **Industrial (INDS)** | `al.industrial@tecmilenio.mx` | Estudiante | Semestre 5 | `DemoMaps2026!` |
| **Mecatrónica (IMTC)** | `al.mecatronica@tecmilenio.mx` | Estudiante | Semestre 6 | `DemoMaps2026!` |
| **Administración (LADM)** | `al.administracion@tecmilenio.mx` | Estudiante | Semestre 4 | `DemoMaps2026!` |
| **Comercio Internacional (LCIN)** | `al.comercio@tecmilenio.mx` | Estudiante | Semestre 5 | `DemoMaps2026!` |
| **Mercadotecnia (LMKT)** | `al.mercadotecnia@tecmilenio.mx` | Estudiante | Semestre 6 | `DemoMaps2026!` |
| **Psicología (LPSI)** | `al.psicologia@tecmilenio.mx` | Estudiante | Semestre 5 | `DemoMaps2026!` |
| **Derecho (LDRC)** | `al.derecho@tecmilenio.mx` | Estudiante | Semestre 8 | `DemoMaps2026!` |
| **Docente (General / Software)** | `al07080561@tecmilenio.mx` *(o `docente1.demo@tecmilenio.mx`)* | Profesor | — | `DemoMaps2026!` |
| **Docentes por carrera** | `docente.industrial@tecmilenio.mx`, `docente.mecatronica@tecmilenio.mx`, etc. | Profesor | — | `DemoMaps2026!` |
| **Administrador** | `admin@tecmilenio.mx` *(o `admin1.demo@tecmilenio.mx`)* | Administrador | — | `DemoMaps2026!` |

## 🧪 Páginas nuevas para verificación visual en demostración

Abrir `http://localhost:8080/api/pages/inicio.html`, iniciar sesión (Ctrl+F5 para tomar las
versiones `?v=` del cache-busting) y revisar:

| Página | URL | Qué verificar |
|--------|-----|---------------|
| **Inicio** | `inicio.html` | Feed personalizado por carrera. Con login de docente: banner de supervisión global. |
| **Semestre Empresarial** | `empresarial.html` | Reseñas completas en empresas top (IBM, Ternium, Bosch, FEMSA, Ryder, PepsiCo, IMSS, BBVA). Ver tarjetas con experiencias destacadas, estrellas y modal de reseñas. |
| **Círculos de Estudio** | `circulos.html` | Círculos permanentes de la carrera en el panel lateral ("Tus grupos permanentes"), sesiones de repaso agendadas para los próximos días con modal para unirse/crear sesiones. |
| **Mi Ruta MAPS** | `certificados.html` | Ruta y catálogo con semestres/materias y botón "Añadir a mi ruta". |

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

## 🚀 Escalabilidad Horizontal y Arquitectura Cluster

MAPS Connect está preparado para ejecutarse tanto como nodo único (desarrollo local sin dependencias extras) como en un **cluster distribuido de alta disponibilidad** detrás de un balanceador de carga (Nginx, AWS ALB, Cloudflare, Kubernetes):

1. **Almacenamiento Desacoplado (`com.tecmilenio.mapsconect.storage`)**:
   - `local`: Guarda archivos de recursos y adjuntos en disco (`uploads/`) con protección path-traversal.
   - `s3`: Compatible con Amazon S3, MinIO o Cloudflare R2 mediante firma nativa AWS SigV4 (sin dependencias pesadas). Permite que cualquier nodo atienda descargas y cargas de archivos sin discrepancias de estado.
2. **Bus de Eventos Distribuido (`com.tecmilenio.mapsconect.messaging`)**:
   - `local`: Emisión en memoria para conexiones SSE en una sola máquina.
   - `redis`: Activa Redis Pub/Sub (`mapsconect:cluster:eventos`). Cuando un estudiante envía un mensaje o notificación en el nodo A, el evento se retransmite a través de Redis para que el nodo B lo empuje inmediatamente al navegador del destinatario vía Server-Sent Events (SSE).
3. **Persistencia y Concurrencia**:
   - Paginación filtrada directamente en motor SQL (`WHERE p.id_materia IN (...)`).
   - Transacciones atómicas (`@Transactional`) para inscripciones concurrentes a sesiones de estudio evitando sobrecupos.
   - Autenticación Stateless basada en JWT, permitiendo balancear peticiones HTTP tipo round-robin sin sticky sessions obligatorias.

## 🔗 Enlaces

- 📖 Backend: `http://localhost:8080/api`
- 🌐 Frontend: `http://localhost:5500`
- ✅ Health: `http://localhost:8080/api/health`

## 👨‍💻 Contacto

**Universidad Tecmilenio** - Proyecto MAPS Connect  
[GitHub Repository](https://github.com/danielsilva7577-bit/maps-conect)

---

**v1.0.0** | Agosto 2024 | En Desarrollo
