# MAPS Connect - Backend API

Plataforma académica y de mentoría MAPS de Universidad Tecmilenio.

## Requisitos Previos

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Configuración de Base de Datos

Crear base de datos antes de ejecutar:

```sql
CREATE DATABASE maps_conect;
CREATE DATABASE maps_conect_dev;
```

Actualizar credenciales en `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/maps_conect
    username: root
    password: tu_contraseña
```

## Compilación

```bash
cd C:\Users\danie\IdeaProjects\maps-conect
mvn clean install
```

## Ejecución

### Desarrollo
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Producción
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## API Health Check

```bash
curl http://localhost:8080/api/health
```

## Estructura del Proyecto

```
src/main/java/com/tecmilenio/mapsconect/
├── config/              # Configuración (CORS, Seguridad, JWT)
├── controller/          # Endpoints REST
├── service/             # Lógica de negocio
├── repository/          # Acceso a datos
├── entity/              # Entidades JPA
├── dto/                 # Transfer Objects
├── exception/           # Manejo de excepciones
├── security/            # JWT y autenticación
└── util/                # Utilidades
```

## Configuración de Seguridad

- JWT Token: Cambiar `jwt.secret` en `application.yml` en producción
- Expiración: 24 horas por defecto
- CORS: Configurado para localhost:3000 y 5173

## Módulos Implementados

✅ Autenticación con JWT
✅ Registro de usuarios
✅ Login
✅ CORS configurado
✅ Manejo global de excepciones
✅ DTOs y validaciones

## Próximos Pasos

1. Crear entidades adicionales (Carrera, Materia, Foro, etc.)
2. Implementar repositorios y servicios
3. Crear controllers para cada módulo
4. Implementar lógica de foros y comunidades
5. Agregar tests

## Contacto

Universidad Tecmilenio - Proyecto MAPS Connect