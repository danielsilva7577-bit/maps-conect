-- Seguir personas + foto de perfil.
-- Idempotente: puede ejecutarse más de una vez.
--
-- IMPORTANTE (Windows): aplicar SIN tubería de PowerShell (corrompe UTF-8).
-- Usar redirección de cmd:
--   cmd /c "mysql -u root -p --default-character-set=utf8mb4 -D maps_conect < seed-follows.sql"
-- La columna foto_url se agrega manualmente si esta versión de MySQL
-- no soporta ADD COLUMN IF NOT EXISTS:
--   ALTER TABLE usuarios ADD COLUMN foto_url LONGTEXT NULL AFTER puntos_reputacion;

CREATE TABLE IF NOT EXISTS seguimientos (
    id_seguimiento INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    id_seguidor INT UNSIGNED NOT NULL,
    id_seguido INT UNSIGNED NOT NULL,
    fecha_seguimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_seguidor_seguido (id_seguidor, id_seguido),
    CONSTRAINT fk_seg_seguidor FOREIGN KEY (id_seguidor) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_seg_seguido FOREIGN KEY (id_seguido) REFERENCES usuarios (id_usuario)
) ENGINE = InnoDB;

-- Compañeros demo para previsualizar la lista "Descubrir personas".
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion)
VALUES
    ('Laura Mariana Torres', 'laura.demo@tecmilenio.mx', 'sin-acceso-demo', 'estudiante', 42),
    ('Diego Ramírez Ponce', 'diego.demo@tecmilenio.mx', 'sin-acceso-demo', 'estudiante', 37),
    ('Ana Sofía Vela Núñez', 'ana.demo@tecmilenio.mx', 'sin-acceso-demo', 'estudiante', 29)
ON DUPLICATE KEY UPDATE nombre_completo = VALUES(nombre_completo), rol = VALUES(rol);

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
SELECT u.id_usuario, 3, 'ALSDEMO01', 4, NULL
FROM usuarios u
WHERE u.correo = 'laura.demo@tecmilenio.mx'
  AND NOT EXISTS (SELECT 1 FROM estudiantes e WHERE e.matricula = 'ALSDEMO01');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
SELECT u.id_usuario, 3, 'DRPDEMO02', 4, NULL
FROM usuarios u
WHERE u.correo = 'diego.demo@tecmilenio.mx'
  AND NOT EXISTS (SELECT 1 FROM estudiantes e WHERE e.matricula = 'DRPDEMO02');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
SELECT u.id_usuario, 3, 'ASVNDEMO03', 4, NULL
FROM usuarios u
WHERE u.correo = 'ana.demo@tecmilenio.mx'
  AND NOT EXISTS (SELECT 1 FROM estudiantes e WHERE e.matricula = 'ASVNDEMO03');

-- Enlaces demo (el alumno real sigue al profesor y a una compañera).
INSERT IGNORE INTO seguimientos (id_seguidor, id_seguido)
SELECT u12.id_usuario, u15.id_usuario FROM usuarios u12, usuarios u15
WHERE u12.correo = 'al07080560@tecmilenio.mx' AND u15.correo = 'profesor.demo@tecmilenio.mx';

INSERT IGNORE INTO seguimientos (id_seguidor, id_seguido)
SELECT u12.id_usuario, uL.id_usuario FROM usuarios u12, usuarios uL
WHERE u12.correo = 'al07080560@tecmilenio.mx' AND uL.correo = 'laura.demo@tecmilenio.mx';