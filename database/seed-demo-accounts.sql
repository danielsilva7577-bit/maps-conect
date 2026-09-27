-- =====================================================================
-- seed-demo-accounts.sql
-- Creación de 10 cuentas para la demo de MAPS Connect:
--   - 5 Cuentas de Administrador
--   - 5 Cuentas de Docente / Profesor con perfil completo y materias
--
-- Credenciales globales:
--   Contraseña común: DemoMaps2026!
--   Hash BCrypt ($2b$12): $2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy
--
-- Ejecución:
--   mysql -u root -p maps_conect < database/seed-demo-accounts.sql
-- =====================================================================

USE maps_conect;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @demo_pwd_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

-- ---------------------------------------------------------------------
-- 1. CINCO CUENTAS DE ADMINISTRADOR
-- ---------------------------------------------------------------------

-- Admin 1: Lic. Roberto Valenzuela (Admin General)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Lic. Roberto Valenzuela', 'admin1.demo@tecmilenio.mx', @demo_pwd_hash, 'administrador', 150, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

-- Admin 2: Mtra. Claudia Méndez (Coordinación Académica)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtra. Claudia Méndez', 'admin2.demo@tecmilenio.mx', @demo_pwd_hash, 'administrador', 150, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

-- Admin 3: Ing. Alejandro Morales (Dirección TI & Plataforma)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Ing. Alejandro Morales', 'admin3.demo@tecmilenio.mx', @demo_pwd_hash, 'administrador', 150, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

-- Admin 4: Lic. Patricia Salgado (Servicios Escolares & Gestión)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Lic. Patricia Salgado', 'admin4.demo@tecmilenio.mx', @demo_pwd_hash, 'administrador', 150, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

-- Admin 5: Dr. Fernando Villarreal (Comité de Innovación Curricular)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dr. Fernando Villarreal', 'admin5.demo@tecmilenio.mx', @demo_pwd_hash, 'administrador', 150, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

-- ---------------------------------------------------------------------
-- 2. CINCO CUENTAS DE DOCENTE / PROFESOR
-- ---------------------------------------------------------------------

-- Docente 1: Dr. Gabriel Navarro Montes
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dr. Gabriel Navarro Montes', 'docente1.demo@tecmilenio.mx', @demo_pwd_hash, 'profesor', 80, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

SET @u_doc1 = (SELECT id_usuario FROM usuarios WHERE correo = 'docente1.demo@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, enlace_sala_virtual, semestres_asignados, disponible_chat)
VALUES (@u_doc1, 'DOC-2026-001', 'Ingeniería de Software y Arquitectura Cloud', 'Especialista en sistemas distribuidos y microservicios con más de 12 años en la industria tecnológica.', 'Lunes y Miércoles 16:00 - 18:00 hrs', 'https://tecmilenio.zoom.us/my/prof.gabriel.navarro', 'Semestre 5, Semestre 6', 1)
ON DUPLICATE KEY UPDATE
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    enlace_sala_virtual = VALUES(enlace_sala_virtual),
    semestres_asignados = VALUES(semestres_asignados),
    disponible_chat = 1;

SET @p_doc1 = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc1);
DELETE FROM profesores_materias WHERE id_profesor = @p_doc1;
INSERT INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc1, 3, '2026-1'), -- Fundamentos de programación
(@p_doc1, 8, '2026-1'); -- POO

-- Docente 2: Mtra. Mariana Treviño Garza
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtra. Mariana Treviño Garza', 'docente2.demo@tecmilenio.mx', @demo_pwd_hash, 'profesor', 95, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

SET @u_doc2 = (SELECT id_usuario FROM usuarios WHERE correo = 'docente2.demo@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, enlace_sala_virtual, semestres_asignados, disponible_chat)
VALUES (@u_doc2, 'DOC-2026-002', 'Ciencia de Datos e Inteligencia Artificial', 'Investigadora en analítica avanzada, machine learning y modelos predictivos aplicados a negocios.', 'Martes y Jueves 15:00 - 17:00 hrs', 'https://tecmilenio.zoom.us/my/prof.mariana.trevino', 'Semestre 4, Semestre 5', 1)
ON DUPLICATE KEY UPDATE
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    enlace_sala_virtual = VALUES(enlace_sala_virtual),
    semestres_asignados = VALUES(semestres_asignados),
    disponible_chat = 1;

SET @p_doc2 = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc2);
DELETE FROM profesores_materias WHERE id_profesor = @p_doc2;
INSERT INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc2, 4, '2026-1'), -- Pensamiento lógico-matemático
(@p_doc2, 9, '2026-1'); -- Estructuras de datos

DELETE FROM profesores_certificados WHERE id_profesor = @p_doc2;
INSERT INTO profesores_certificados (id_profesor, id_certificado) VALUES
(@p_doc2, 24); -- Certificado de Ciencia de Datos Empresarial

-- Docente 3: Ing. Héctor Ramírez Alarcón
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Ing. Héctor Ramírez Alarcón', 'docente3.demo@tecmilenio.mx', @demo_pwd_hash, 'profesor', 75, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

SET @u_doc3 = (SELECT id_usuario FROM usuarios WHERE correo = 'docente3.demo@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, enlace_sala_virtual, semestres_asignados, disponible_chat)
VALUES (@u_doc3, 'DOC-2026-003', 'Ciberseguridad y Redes Corporativas', 'Consultor en seguridad de infraestructuras críticas y auditoría de vulnerabilidades con certificación CISSP.', 'Viernes 14:00 - 18:00 hrs', 'https://tecmilenio.zoom.us/my/prof.hector.ramirez', 'Semestre 6, Semestre 7', 1)
ON DUPLICATE KEY UPDATE
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    enlace_sala_virtual = VALUES(enlace_sala_virtual),
    semestres_asignados = VALUES(semestres_asignados),
    disponible_chat = 1;

SET @p_doc3 = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc3);
DELETE FROM profesores_materias WHERE id_profesor = @p_doc3;
INSERT INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc3, 11, '2026-1'); -- Fundamentos de redes y sistemas operativos

-- Docente 4: Dra. Sofía Castellanos Ruiz
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dra. Sofía Castellanos Ruiz', 'docente4.demo@tecmilenio.mx', @demo_pwd_hash, 'profesor', 110, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

SET @u_doc4 = (SELECT id_usuario FROM usuarios WHERE correo = 'docente4.demo@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, enlace_sala_virtual, semestres_asignados, disponible_chat)
VALUES (@u_doc4, 'DOC-2026-004', 'Desarrollo Web Full Stack y Diseño UI/UX', 'Líder técnica en ingeniería de aplicaciones web modernas, frameworks reactivos y experiencia de usuario.', 'Lunes y Jueves 17:00 - 19:00 hrs', 'https://tecmilenio.zoom.us/my/prof.sofia.castellanos', 'Semestre 3, Semestre 4', 1)
ON DUPLICATE KEY UPDATE
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    enlace_sala_virtual = VALUES(enlace_sala_virtual),
    semestres_asignados = VALUES(semestres_asignados),
    disponible_chat = 1;

SET @p_doc4 = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc4);
DELETE FROM profesores_materias WHERE id_profesor = @p_doc4;
INSERT INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc4, 8, '2026-1'); -- POO

DELETE FROM profesores_certificados WHERE id_profesor = @p_doc4;
INSERT INTO profesores_certificados (id_profesor, id_certificado) VALUES
(@p_doc4, 23); -- Certificado de Desarrollo Web Front-End

-- Docente 5: Mtro. Ricardo Peña Delgado
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtro. Ricardo Peña Delgado', 'docente5.demo@tecmilenio.mx', @demo_pwd_hash, 'profesor', 85, 1)
ON DUPLICATE KEY UPDATE
    nombre_completo = VALUES(nombre_completo),
    contrasena_hash = VALUES(contrasena_hash),
    rol = VALUES(rol),
    activo = 1;

SET @u_doc5 = (SELECT id_usuario FROM usuarios WHERE correo = 'docente5.demo@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, enlace_sala_virtual, semestres_asignados, disponible_chat)
VALUES (@u_doc5, 'DOC-2026-005', 'Liderazgo, Bienestar y Gestión Ágil', 'Coach ejecutivo y Scrum Master certificado, enfocado en el desarrollo de competencias de alto impacto y comunicación.', 'Miércoles y Viernes 10:00 - 12:00 hrs', 'https://tecmilenio.zoom.us/my/prof.ricardo.pena', 'Semestre 5, Semestre 6', 1)
ON DUPLICATE KEY UPDATE
    area_especialidad = VALUES(area_especialidad),
    biografia = VALUES(biografia),
    horario_asesorias = VALUES(horario_asesorias),
    enlace_sala_virtual = VALUES(enlace_sala_virtual),
    semestres_asignados = VALUES(semestres_asignados),
    disponible_chat = 1;

SET @p_doc5 = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc5);
DELETE FROM profesores_materias WHERE id_profesor = @p_doc5;
INSERT INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc5, 5, '2026-1'), -- Habilidades de bienestar y liderazgo
(@p_doc5, 6, '2026-1'); -- Comunicación efectiva
