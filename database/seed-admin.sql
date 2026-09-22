-- =====================================================================
-- seed-admin.sql
-- Cuenta de administrador para MAPS Connect.
-- Idempotente: se puede ejecutar más de una vez sin duplicar.
--
-- Credenciales:
--   Email:    admin@tecmilenio.mx
--   Password:  DemoMaps2026!
--   Rol:       administrador
--
-- Uso:
--   mysql -u root -p maps_conect < database/seed-admin.sql
-- =====================================================================
USE maps_conect;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- BCrypt hash de "DemoMaps2026!" (cost factor 10). Compatibilidad con
-- Spring Security BCryptPasswordEncoder (JBCrypt). Verificado con Python bcrypt.
SET @admin_password_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

INSERT INTO usuarios (id_usuario, nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, foto_url, activo)
VALUES (13, 'Prof. Martin Silva', 'admin@tecmilenio.mx', @admin_password_hash, 'administrador', 100, NULL, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo   = VALUES(nombre_completo),
    contrasena_hash   = VALUES(contrasena_hash),
    rol               = VALUES(rol),
    puntos_reputacion = VALUES(puntos_reputacion),
    foto_url          = VALUES(foto_url),
    activo            = 1;

-- Confirmación visual
SELECT id_usuario, nombre_completo, correo, rol, activo
FROM usuarios
WHERE correo = 'admin@tecmilenio.mx';
