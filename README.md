# MAPS Connect - Backend API

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/)

**MAPS Connect** es una plataforma académica y de mentoría integral para la Universidad Tecmilenio que conecta estudiantes, docentes y egresados mediante un sistema colaborativo de redes de conocimiento, círculos de estudio y asesorías personalizadas.

## 📋 Tabla de Contenidos

- [Visión General](#visión-general)
- [Características Principales](#características-principales)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Módulos Funcionales](#módulos-funcionales)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Endpoints de API](#endpoints-de-api)
- [Seguridad y Autenticación](#seguridad-y-autenticación)
- [Base de Datos](#base-de-datos)
- [Desarrollo](#desarrollo)
- [Contacto](#contacto)

## 🎯 Visión General

En el modelo educativo de Universidad Tecmilenio, los estudiantes del programa **MAPS (Modelo de Aprendizaje Personalizado y Significativo)** combinan materias disciplinares, componentes de bienestar, certificados de especialización y participan en el Semestre Empresarial. Sin embargo, frecuentemente enfrentan:

- ❌ Incertidumbre al seleccionar certificados especializados
- ❌ Dificultad para resolver dudas académicas específicas
- ❌ Falta de grupos de estudio organizados
- ❌ Desconocimiento sobre opciones de empresas para vinculación

**MAPS Connect** resuelve estos desafíos centralizando el intercambio de conocimiento académico, fomentando el aprendizaje colaborativo y facilitando decisiones curriculares informadas.

## ✨ Características Principales

### 🔐 Autenticación y Usuarios
- Registro seguro mediante correo institucional (@tecmilenio.mx)
- Autenticación con **JWT (JSON Web Tokens)**
- Perfiles especializados: **Estudiantes**, **Profesores** y **Administradores**
- Gestión de credenciales y sesiones seguras
- Validación de dominios institucionales

### 💬 Foro Colaborativo
- Dudas académicas segmentadas por carrera y materia
- Respuestas de la comunidad con sistema de soluciones verificadas
- Insignias automáticas para aportaciones de docentes
- Reputación basada en participación

### 💡 Tips Académicos
- Microconsejos y metodologías de estudio por materia
- Sistema de votación única por usuario
- Ranking dinámico de mejores consejos
- Contenido enfocado en técnicas probadas

### 📚 Repositorio de Recursos
- Biblioteca centralizada de apuntes y material (PDFs, diagramas, resúmenes)
- Clasificación por materia y tema
- Control de descargas y reportes
- Validación automática de cumplimiento comunitario

### 👥 Círculos de Estudio
- Comunidades colaborativas por materia, certificado o carrera
- Privacidad configurable (pública/privada)
- Integración con salas virtuales (Microsoft Teams/Google Meet)
- Muro interno de avisos y discusiones

### 💌 Mensajería Privada
- Canal 1 a 1 entre estudiantes o profesor-estudiante
- Asesorías académicas personalizadas
- Trazabilidad completa de conversaciones
- Respeto a disponibilidad horaria docente

### 🏢 Semestre Empresarial
- Directorio de empresas vinculadas
- Reseñas y evaluaciones de experiencias reales
- Consejos de postulación y acreditación
- Retroalimentación de estudiantes avanzados

### 🎓 Gestión Académica MAPS
- Catálogo de carreras y certificados de especialización
- Estructura de malla curricular por programa
- Materias tronco común, disciplinares, certificados y bienestar
- Modelado de microcredenciales

## 📦 Requisitos Previos

- **Java 25** o superior
- **Maven 3.8** o superior
- **MySQL 8.0** o superior
- **Git** para control de versiones
- **IDE** recomendado: IntelliJ IDEA Community Edition

### Validar Instalación

```bash
java -version
mvn -version
mysql --version
```

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone https://github.com/danielsilva7577-bit/maps-conect.git
cd maps-conect
```

### 2. Crear Bases de Datos

```sql
CREATE DATABASE maps_conect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE maps_conect_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar Credenciales

Editar `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: MAPS Connect
  
  datasource:
    url: jdbc:mysql://localhost:3306/maps_conect
    username: root
    password: tu_contraseña
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

server:
  port: 8080
  servlet:
    context-path: /api

jwt:
  secret: tu_clave_secreta_muy_segura_aqui
  expiration: 86400000  # 24 horas en milisegundos

cors:
  allowed-origins: http://localhost:3000,http://localhost:5173
```

### 4. Compilar el Proyecto

```bash
mvn clean install
```

### 5. Ejecutar la Aplicación

**Desarrollo (con perfil dev):**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Producción (con perfil prod):**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

La aplicación estará disponible en: `http://localhost:8080/api`

### 6. Verificar Salud de la API

```bash
curl http://localhost:8080/api/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## 🏗️ Arquitectura del Sistema

MAPS Connect implementa una **arquitectura de 3 capas**:

### Capa de Presentación (Frontend)
- **Tecnologías**: HTML5, CSS3, JavaScript Vanilla
- **Responsabilidades**:
  - Renderizado dinámico del DOM
  - Interfaz minimalista con contornos rectos
  - Menú colapsable
  - Validación de formularios en cliente
  - Consumo asincrónico de endpoints REST vía JSON

### Capa de Aplicación (Backend)
- **Tecnologías**: Java, Spring Boot, Spring Security
- **Responsabilidades**:
  - Implementación de reglas de negocio
  - Validación de dominios institucionales
  - Manejo de sesiones y seguridad
  - Orquestación de servicios y repositorios
  - Procesamiento de requests y responses

### Capa de Datos (Base de Datos)
- **Tecnologías**: MySQL 8.0
- **Responsabilidades**:
  - Almacenamiento persistente normalizado en 3FN
  - Integridad referencial con claves foráneas
  - Restricciones de dominio y lógica
  - Vistas operativas para consultas complejas

```
┌─────────────────────────────────┐
│     Frontend (Web Browser)      │
│  HTML5 | CSS3 | JavaScript      │
└──────────────┬──────────────────┘
               │ REST JSON
┌──────────────▼──────────────────┐
│    Spring Boot REST API         │
│ Controllers | Services | DAO    │
│  JWT Security | Validation      │
└──────────────┬──────────────────┘
               │ SQL
┌──────────────▼──────────────────┐
│    MySQL 8.0 Database           │
│  22 Tablas | 3FN | Constraints  │
└─────────────────────────────────┘
```

## 📚 Módulos Funcionales

### 1️⃣ Gestión Académica y Modelo MAPS
Administra el catálogo académico de la institución.

**Entidades**:
- `carreras` - Programas profesionales ofertados
- `certificados` - Especializaciones y microcredenciales MAPS
- `materias` - Asignaturas curriculares
- `plan_estudios` - Relación N:M de materias por carrera/semestre
- `certificado_materias` - Asignaturas que integran certificados

**Funcionalidades**:
- ✅ Definir carreras y planes de estudio
- ✅ Configurar certificados de especialización
- ✅ Asignar materias a carreras y certificados
- ✅ Estructurar malla curricular

### 2️⃣ Usuarios, Perfiles y Roles
Administración integral de credenciales e identidades.

**Entidades**:
- `usuarios` - Credenciales y autenticación
- `estudiantes` - Datos específicos del alumno (matrícula, semestre, propósito)
- `estudiante_certificados` - Ruta MAPS (hasta 3 certificados)
- `profesores` - Datos de nómina, especialidad y horarios
- `profesores_materias` - Relación N:M de materias impartidas

**Roles y Permisos**:
| Operación | Estudiante | Profesor | Admin |
|-----------|-----------|----------|-------|
| Editar Perfil | ✅ | ✅ | ✅ |
| Publicar Duda | ✅ | ❌ | 🔧 |
| Responder Dudas | ✅ | ✅ (con insignia) | 🔧 |
| Crear Círculos | ✅ | ✅ (Líder) | 🔧 |
| Moderación | ❌ | ✅ | ✅ |
| Administrar Catálogos | ❌ | ❌ | ✅ |

### 3️⃣ Foro de Dudas y Respuestas Colaborativas
Canal principal de interacción académica.

**Entidades**:
- `publicaciones` - Dudas académicas por carrera/materia
- `respuestas` - Soluciones y comentarios de la comunidad

**Características**:
- Dudas asociadas a materias (opcional)
- Comunidad aporta respuestas
- Autor marca solución definitiva
- Sistema automático de insignias para docentes
- Ranking de respuestas por utilidad

### 4️⃣ Tips Académicos y Sistema de Reputación
Microconsejos y metodologías de estudio.

**Entidades**:
- `tips_academicos` - Recomendaciones y técnicas
- `votos_tips` - Control de votos únicos por usuario

**Características**:
- Tips por materia
- Voto único por usuario (evita manipulación)
- Sistema de reputación basado en contribuciones
- Feed ranking por utilidad

### 5️⃣ Repositorio de Apuntes y Material de Estudio
Biblioteca centralizada de recursos.

**Entidades**:
- `recursos_academicos` - Metadatos de archivos y apuntes

**Características**:
- Clasificación por materia
- Monitoreo de descargas
- Reportes de calidad
- Ocultamiento automático de material no conforme

### 6️⃣ Mensajería Directa y Asesorías Privadas
Canal 1 a 1 para comunicación personalizada.

**Entidades**:
- `conversaciones` - Canales 1 a 1
- `mensajes` - Mensajes individuales

**Características**:
- Entre estudiantes o estudiante-profesor
- Trazabilidad completa
- Confirmación de lectura
- Respeto a disponibilidad docente

### 7️⃣ Semestre Empresarial
Retroalimentación y experiencias de vinculación.

**Entidades**:
- `empresas_vinculadas` - Directorio de empresas
- `resenas_empresarial` - Evaluaciones y aprendizajes

**Características**:
- Reseñas de experiencias reales
- Evaluación de 1 a 5 estrellas
- Consejos de postulación
- Recomendaciones de acreditación

### 8️⃣ Comunidades y Círculos de Estudio
Espacios colaborativos para aprendizaje grupal.

**Entidades**:
- `comunidades_estudio` - Grupos de estudio
- `miembros_comunidad` - Relación N:M de usuarios
- `mensajes_comunidad` - Muro de publicaciones internas

**Características**:
- Por materia, certificado o carrera
- Privacidad configurable (pública/privada)
- Integración con salas virtuales (Teams/Meet)
- Muro interno de avisos
- Liderazgo y modración

## 📂 Estructura del Proyecto

```
maps-conect/
├── src/
│   ├── main/
│   │   ├── java/com/tecmilenio/mapsconect/
│   │   │   ├── MapsConectApplication.java          # Punto de entrada
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                 # Configuración CORS
│   │   │   │   └── SecurityConfig.java             # Configuración seguridad
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java             # Endpoints autenticación
│   │   │   │   └── HealthController.java           # Health check
│   │   │   │
│   │   │   ├── service/
│   │   │   │   └── UsuarioService.java             # Lógica usuarios
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   └── UsuarioRepository.java          # Acceso a datos
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   └── Usuario.java                    # Entidades JPA
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── LoginDTO.java
│   │   │   │   ├── RegistroDTO.java
│   │   │   │   ├── TokenDTO.java
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   └── ApiResponse.java
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java           # Generación JWT
│   │   │   │   └── JwtAuthenticationFilter.java    # Filtro JWT
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java     # Manejo global excepciones
│   │   │   │   └── ResourceNotFoundException.java  # Excepciones custom
│   │   │   │
│   │   │   └── util/
│   │   │       └── (utilidades compartidas)
│   │   │
│   │   └── resources/
│   │       ├── application.yml                      # Config principal
│   │       ├── application-dev.yml                  # Config desarrollo
│   │       └── application-prod.yml                 # Config producción
│   │
│   └── test/
│       └── java/com/tecmilenio/mapsconect/
│           └── (tests unitarios e integración)
│
├── .gitignore
├── pom.xml                                          # Dependencias Maven
├── README.md                                        # Este archivo
└── target/                                          # Artifacts compilados
```

## 🔌 Endpoints de API

### Autenticación
```http
POST /api/auth/register
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan.perez@tecmilenio.mx",
  "password": "Password123!",
  "rol": "STUDENT"
}
```

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan.perez@tecmilenio.mx",
  "password": "Password123!"
}
```

**Respuesta (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "expiresIn": 86400000,
  "usuario": {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan.perez@tecmilenio.mx",
    "rol": "STUDENT"
  }
}
```

### Health Check
```http
GET /api/health
```

**Respuesta**:
```json
{
  "status": "UP",
  "timestamp": "2024-08-17T18:00:00Z"
}
```

## 🔐 Seguridad y Autenticación

### JWT (JSON Web Tokens)

La aplicación utiliza **JWT** para autenticación stateless:

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "usuario@tecmilenio.mx",
  "id": 1,
  "rol": "STUDENT",
  "iat": 1692291600,
  "exp": 1692378000
}

Signature:
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### Restricciones de Seguridad

✅ **Dominio Institucional**: Solo correos @tecmilenio.mx o @servicios.tecmilenio.mx  
✅ **Contraseñas Hasheadas**: Almacenadas con bcrypt (algoritmo seguro)  
✅ **Expiración de Token**: 24 horas por defecto  
✅ **CORS**: Restringido a orígenes permitidos  
✅ **Validación de Entrada**: DTOs con @Valid annotations  
✅ **Manejo Global de Excepciones**: Respuestas consistentes

### Uso del Token

Incluir en todas las requests:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Cambiar Secreto en Producción

**CRÍTICO**: Cambiar `jwt.secret` en `application-prod.yml`:

```yaml
jwt:
  secret: una_clave_secreta_aleatoria_muy_larga_y_compleja
  expiration: 86400000
```

Generar clave segura:
```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

## 💾 Base de Datos

### Modelado en 3FN (Tercera Forma Normal)

La base de datos implementa 3FN para garantizar:
- ✅ Normalización completa
- ✅ Eliminación de redundancias
- ✅ Integridad referencial
- ✅ Eficiencia en consultas

### 22 Entidades Principales

| ID | Entidad | Módulo | Propósito |
|----|---------|--------|-----------|
| 1 | `carreras` | Académico | Programas profesionales |
| 2 | `certificados` | Académico | Microcredenciales MAPS |
| 3 | `materias` | Académico | Asignaturas curriculares |
| 4 | `plan_estudios` | Académico | Relación materia-carrera |
| 5 | `certificado_materias` | Académico | Materias por certificado |
| 6 | `usuarios` | Usuarios | Credenciales y autenticación |
| 7 | `estudiantes` | Usuarios | Datos del alumno |
| 8 | `estudiante_certificados` | Usuarios | Ruta MAPS del alumno |
| 9 | `profesores` | Usuarios | Datos docentes |
| 10 | `profesores_materias` | Usuarios | Materias por docente |
| 11 | `publicaciones` | Comunidad | Dudas académicas |
| 12 | `respuestas` | Comunidad | Respuestas y comentarios |
| 13 | `tips_academicos` | Tips | Microconsejos de estudio |
| 14 | `votos_tips` | Tips | Votos únicos por usuario |
| 15 | `recursos_academicos` | Apuntes | Metadatos de archivos |
| 16 | `conversaciones` | Chat | Canales 1 a 1 |
| 17 | `mensajes` | Chat | Mensajes individuales |
| 18 | `empresas_vinculadas` | Empresarial | Directorio de empresas |
| 19 | `resenas_empresarial` | Empresarial | Evaluaciones de empresas |
| 20 | `comunidades_estudio` | Círculos | Grupos de estudio |
| 21 | `miembros_comunidad` | Círculos | Usuarios en comunidades |
| 22 | `mensajes_comunidad` | Círculos | Muro de comunidades |

### Restricciones Técnicas

**1. Restricción de Dominio Institucional**
```sql
ALTER TABLE usuarios ADD CONSTRAINT chk_email_domain 
CHECK (email LIKE '%@tecmilenio.mx' OR email LIKE '%@servicios.tecmilenio.mx');
```

**2. Restricción de Rango Semestral**
```sql
ALTER TABLE estudiantes ADD CONSTRAINT chk_semestre 
CHECK (semestre_actual BETWEEN 1 AND 12);
```

**3. Límite de Ruta Curricular**
```sql
ALTER TABLE estudiantes ADD CONSTRAINT chk_max_certificados 
CHECK (LENGTH(CONCAT_WS(',', certificado_1, certificado_2, certificado_3)) > 0);
```

**4. Integridad en Mensajería**
```sql
ALTER TABLE conversaciones ADD CONSTRAINT chk_usuarios_distintos 
CHECK (id_usuario_1 <> id_usuario_2);
ALTER TABLE conversaciones ADD CONSTRAINT uq_usuarios 
UNIQUE KEY (id_usuario_1, id_usuario_2);
```

**5. Calificaciones Numéricas (1-5)**
```sql
ALTER TABLE resenas_empresarial ADD CONSTRAINT chk_calificacion 
CHECK (calificacion BETWEEN 1 AND 5);
```

**6. Integridad en Cascada**
```sql
ALTER TABLE miembros_comunidad ADD CONSTRAINT fk_comunidad 
FOREIGN KEY (id_comunidad) REFERENCES comunidades_estudio(id)
ON DELETE CASCADE;
```

## 🛠️ Desarrollo

### Requisitos de Desarrollo

```bash
# Verificar Java
java -version
# Debe mostrar Java 25+

# Verificar Maven
mvn -version
# Debe mostrar Maven 3.8+

# Verificar MySQL
mysql --version
# Debe mostrar MySQL 8.0+
```

### Compilación

**Build completo (ejecutar una sola vez)**:
```bash
mvn clean install
```

**Build rápido (cambios menores)**:
```bash
mvn compile
```

**Limpiar build anterior**:
```bash
mvn clean
```

### Ejecutar con Perfil Dev

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Verá logs detallados y SQL ejecutado.

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Test específico
mvn test -Dtest=UsuarioServiceTest

# Con cobertura
mvn test jacoco:report
```

### Empaquetar para Producción

```bash
mvn package
# Crea: target/maps-conect-1.0.0.jar
```

**Ejecutar JAR**:
```bash
java -Dspring.profiles.active=prod -jar target/maps-conect-1.0.0.jar
```

### IDE Recomendado: IntelliJ IDEA

1. Abrir proyecto: `File > Open > maps-conect`
2. Configurar SDK Java 25: `File > Project Structure > Project > SDK`
3. Configurar Maven: `File > Settings > Build Tools > Maven`
4. Ejecutar aplicación: `Run > Run 'MapsConectApplication'`
5. Debug: `Run > Debug 'MapsConectApplication'`

### Variables de Entorno

Para producción, usar variables de entorno en lugar de archivos properties:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/maps_conect
export SPRING_DATASOURCE_USERNAME=user_prod
export SPRING_DATASOURCE_PASSWORD=secure_password
export JWT_SECRET=your_production_secret_key
export SPRING_PROFILES_ACTIVE=prod

java -jar maps-conect-1.0.0.jar
```

### Estructura de Carpeta de Trabajo

```
C:\Users\danie\
├── IdeaProjects/
│   ├── maps-conect/              # Este proyecto
│   └── ...
├── .m2/                          # Maven local repository
└── ...
```

## 📞 Contacto

**Institución**: Universidad Tecmilenio  
**Proyecto**: MAPS Connect - Plataforma Académica y de Mentoría  
**Repositorio**: [GitHub - danielsilva7577-bit/maps-conect](https://github.com/danielsilva7577-bit/maps-conect)  

---

**Última actualización**: Agosto 2024  
**Versión**: 1.0.0  
**Estado**: En Desarrollo