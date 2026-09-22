-- Cuentas de verificación para una base maps_conect existente.
-- Idempotente: se puede ejecutar más de una vez.
USE maps_conect;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- DemoMaps2026!
SET @test_password_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';
-- DemoMaps2026!
SET @berlin_password_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Docente Demo MAPS', 'al07080561@tecmilenio.mx', @test_password_hash, 'profesor', 10, 1)
ON DUPLICATE KEY UPDATE
    id_usuario = LAST_INSERT_ID(id_usuario),
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;
SET @docente_usuario = LAST_INSERT_ID();

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@docente_usuario, 'NOM-07080561', 'Ingeniería de Software',
        'Docente de demostración para MAPS Connect.', 'Lunes a viernes, 16:00 a 18:00', 1)
ON DUPLICATE KEY UPDATE
    id_profesor = LAST_INSERT_ID(id_profesor),
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    disponible_chat = VALUES(disponible_chat);

INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Estudiante Semestre 6 Demo', 'al07080562@tecmilenio.mx', @test_password_hash, 'estudiante', 10, 1)
ON DUPLICATE KEY UPDATE
    id_usuario = LAST_INSERT_ID(id_usuario),
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;
SET @semestre6_usuario = LAST_INSERT_ID();

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@semestre6_usuario, 3, 'AL07080562', 6,
        'Aplicar mis conocimientos en un proyecto de Semestre Empresarial.')
ON DUPLICATE KEY UPDATE
    id_estudiante = LAST_INSERT_ID(id_estudiante),
    id_carrera = VALUES(id_carrera),
    semestre_actual = VALUES(semestre_actual),
    proposito_vida = VALUES(proposito_vida);
SET @semestre6_estudiante = LAST_INSERT_ID();

INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion)
SELECT @semestre6_estudiante, id_materia, NOW()
FROM materias
WHERE id_materia IN (24, 25, 26);

UPDATE usuarios
SET contrasena_hash = @berlin_password_hash, activo = 1
WHERE correo = 'semestre.empresarial.demo@tecmilenio.mx';
