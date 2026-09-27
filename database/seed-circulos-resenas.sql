-- =====================================================================
-- seed-circulos-resenas.sql
-- Carga integral de Círculos de Estudio, Sesiones de Repaso
-- y Reseñas de Semestre Empresarial para TODAS las carreras de MAPS Connect.
--
-- Idempotente: puede ejecutarse de manera independiente o como complemento.
-- =====================================================================

USE maps_conect;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @pwd_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

-- =====================================================================
-- 1. ASEGURAR SEMESTRE >= 7 EN ESTUDIANTES DEMO
-- (Requerido por el trigger trg_resenas_empresarial_semestre_minimo)
-- =====================================================================

UPDATE estudiantes e
JOIN usuarios u ON u.id_usuario = e.id_usuario
SET e.semestre_actual = 7
WHERE u.correo IN (
    'al.industrial@tecmilenio.mx',
    'al.mecatronica@tecmilenio.mx',
    'al.comercio@tecmilenio.mx',
    'al.psicologia@tecmilenio.mx',
    'al07080560@tecmilenio.mx'
);

UPDATE estudiantes e
JOIN usuarios u ON u.id_usuario = e.id_usuario
SET e.semestre_actual = 8
WHERE u.correo IN (
    'al.software@tecmilenio.mx',
    'al.administracion@tecmilenio.mx',
    'al.mercadotecnia@tecmilenio.mx',
    'al.derecho@tecmilenio.mx'
);

-- =====================================================================
-- 2. COMPAÑEROS SENIOR ADICIONALES (Para grupos e inscripciones realistas)
-- =====================================================================

-- Software Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Gabriel Rivas Morales', 'gabriel.dev@tecmilenio.mx', @pwd_hash, 'estudiante', 92, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_gab = (SELECT id_usuario FROM usuarios WHERE correo = 'gabriel.dev@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_gab, 3, 'AL03002002', 8, 'Desarrollar arquitecturas cloud nativas de alto rendimiento.')
ON DUPLICATE KEY UPDATE id_carrera=3, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_gab = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_gab);

-- Industrial Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Valeria Garza Benavides', 'valeria.ind@tecmilenio.mx', @pwd_hash, 'estudiante', 88, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_val_ind = (SELECT id_usuario FROM usuarios WHERE correo = 'valeria.ind@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_val_ind, 4, 'AL04002002', 8, 'Liderar la transformación digital de plantas de manufactura.')
ON DUPLICATE KEY UPDATE id_carrera=4, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_val_ind = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_val_ind);

-- Mecatrónica Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Carlos Treviño Elizondo', 'carlos.meca@tecmilenio.mx', @pwd_hash, 'estudiante', 95, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_car_mec = (SELECT id_usuario FROM usuarios WHERE correo = 'carlos.meca@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_car_mec, 5, 'AL05002002', 8, 'Diseñar celdas robóticas seguras y eficientes para el sector automotriz.')
ON DUPLICATE KEY UPDATE id_carrera=5, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_car_mec = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_car_mec);

-- Administración Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Paola Villarreal Serna', 'paola.admin@tecmilenio.mx', @pwd_hash, 'estudiante', 84, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_pao_adm = (SELECT id_usuario FROM usuarios WHERE correo = 'paola.admin@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_pao_adm, 6, 'AL06002002', 7, 'Crear estrategias financieras sostenibles con enfoque ESG.')
ON DUPLICATE KEY UPDATE id_carrera=6, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_pao_adm = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_pao_adm);

-- Comercio Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Diego Salgado Leal', 'diego.comercio@tecmilenio.mx', @pwd_hash, 'estudiante', 82, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_die_com = (SELECT id_usuario FROM usuarios WHERE correo = 'diego.comercio@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_die_com, 7, 'AL07002002', 8, 'Optimizar corredores logísticos intermodales en América del Norte.')
ON DUPLICATE KEY UPDATE id_carrera=7, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_die_com = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_die_com);

-- Mercadotecnia Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Renata Quiroga Domínguez', 'renata.mkt@tecmilenio.mx', @pwd_hash, 'estudiante', 90, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_ren_mkt = (SELECT id_usuario FROM usuarios WHERE correo = 'renata.mkt@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_ren_mkt, 8, 'AL08002002', 7, 'Posicionar marcas con impacto social y campañas basadas en analítica.')
ON DUPLICATE KEY UPDATE id_carrera=8, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_ren_mkt = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_ren_mkt);

-- Psicología Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Andrea Balderas Tamez', 'andrea.psi@tecmilenio.mx', @pwd_hash, 'estudiante', 94, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_and_psi = (SELECT id_usuario FROM usuarios WHERE correo = 'andrea.psi@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_and_psi, 9, 'AL09002002', 8, 'Desarrollar programas de salud mental y bienestar en organizaciones.')
ON DUPLICATE KEY UPDATE id_carrera=9, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_and_psi = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_and_psi);

-- Derecho Senior
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Sebastián Cárdenas Guerra', 'sebastian.leyes@tecmilenio.mx', @pwd_hash, 'estudiante', 96, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_seb_der = (SELECT id_usuario FROM usuarios WHERE correo = 'sebastian.leyes@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_seb_der, 10, 'AL10002002', 7, 'Especializarme en derecho corporativo, bancario y litigio mercantil.')
ON DUPLICATE KEY UPDATE id_carrera=10, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_seb_der = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_seb_der);

-- Cargar variables de usuarios principales
SET @u_soft = (SELECT id_usuario FROM usuarios WHERE correo = 'lucia.mendez@tecmilenio.mx');
SET @e_soft = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_soft);

SET @u_ind = (SELECT id_usuario FROM usuarios WHERE correo = 'al.industrial@tecmilenio.mx');
SET @e_ind = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_ind);

SET @u_mec = (SELECT id_usuario FROM usuarios WHERE correo = 'al.mecatronica@tecmilenio.mx');
SET @e_mec = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_mec);

SET @u_adm = (SELECT id_usuario FROM usuarios WHERE correo = 'al.administracion@tecmilenio.mx');
SET @e_adm = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_adm);

SET @u_com = (SELECT id_usuario FROM usuarios WHERE correo = 'al.comercio@tecmilenio.mx');
SET @e_com = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_com);

SET @u_mkt = (SELECT id_usuario FROM usuarios WHERE correo = 'al.mercadotecnia@tecmilenio.mx');
SET @e_mkt = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_mkt);

SET @u_psi = (SELECT id_usuario FROM usuarios WHERE correo = 'al.psicologia@tecmilenio.mx');
SET @e_psi = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_psi);

SET @u_der = (SELECT id_usuario FROM usuarios WHERE correo = 'al.derecho@tecmilenio.mx');
SET @e_der = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_der);

SET @u_daniel = (SELECT id_usuario FROM usuarios WHERE correo = 'al07080560@tecmilenio.mx');
SET @e_daniel = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_daniel);

-- =====================================================================
-- 3. COMUNIDADES PERMANENTES DE ESTUDIO (CÍRCULOS)
-- =====================================================================

-- Software (Carrera 3)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Arquitectura de Software y Algoritmos', @u_soft, 3, 2, 'publica', 'https://tecmilenio.zoom.us/j/c-software-arq')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio Web y Mobile Full-Stack', @u_gab, 3, 4, 'publica', 'https://tecmilenio.zoom.us/j/c-software-web')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Industrial (Carrera 4)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Seis Sigma y Calidad Industrial', @u_ind, 4, 44, 'publica', 'https://tecmilenio.zoom.us/j/c-industrial-calidad')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Manufactura Esbelta & Kaizen', @u_val_ind, 4, 46, 'publica', 'https://tecmilenio.zoom.us/j/c-industrial-lean')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Mecatrónica (Carrera 5)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio de Programación PLC y Robótica', @u_mec, 5, 72, 'publica', 'https://tecmilenio.zoom.us/j/c-mecatronica-plc')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Sistemas Embebidos e IoT Industrial', @u_car_mec, 5, 75, 'publica', 'https://tecmilenio.zoom.us/j/c-mecatronica-iot')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Administración (Carrera 6)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Seminario de Finanzas Corporativas y DCF', @u_adm, 6, 97, 'publica', 'https://tecmilenio.zoom.us/j/c-administracion-finanzas')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Incubadora de Modelos de Negocio y Startups', @u_pao_adm, 6, 100, 'publica', 'https://tecmilenio.zoom.us/j/c-administracion-modelos')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Comercio Internacional (Carrera 7)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Taller de Aduanas y Comercio Exterior T-MEC', @u_com, 7, 119, 'publica', 'https://tecmilenio.zoom.us/j/c-comercio-aduanas')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Logística Internacional y Cadena de Suministro', @u_die_com, 7, 121, 'publica', 'https://tecmilenio.zoom.us/j/c-comercio-logistica')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Mercadotecnia (Carrera 8)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Growth Marketing & Estrategia Digital', @u_mkt, 8, 140, 'publica', 'https://tecmilenio.zoom.us/j/c-mercadotecnia-digital')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio de Branding e Insights del Consumidor', @u_ren_mkt, 8, 143, 'publica', 'https://tecmilenio.zoom.us/j/c-mercadotecnia-branding')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Psicología (Carrera 9)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Club de Casos Clínicos y Psicometría', @u_psi, 9, 163, 'publica', 'https://tecmilenio.zoom.us/j/c-psicologia-clinica')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Psicología Organizacional y NOM-035', @u_and_psi, 9, 166, 'publica', 'https://tecmilenio.zoom.us/j/c-psicologia-organizacional')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Derecho (Carrera 10)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Sociedad de Litigio y Juicios Orales', @u_der, 10, 187, 'publica', 'https://tecmilenio.zoom.us/j/c-derecho-oralidad')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Amparo y Garantías Constitucionales', @u_seb_der, 10, 188, 'publica', 'https://tecmilenio.zoom.us/j/c-derecho-amparo')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- =====================================================================
-- 4. MIEMBROS DE LAS COMUNIDADES (Para poblar "Tus grupos permanentes")
-- =====================================================================

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_soft, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Arquitectura de Software y Algoritmos';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_gab, 'asesor', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Arquitectura de Software y Algoritmos';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_daniel, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Arquitectura de Software y Algoritmos';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_soft, 'miembro', NOW() - INTERVAL 15 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio Web y Mobile Full-Stack';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_gab, 'lider', NOW() - INTERVAL 30 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio Web y Mobile Full-Stack';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_daniel, 'miembro', NOW() - INTERVAL 10 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio Web y Mobile Full-Stack';

-- Industrial
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ind, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Seis Sigma y Calidad Industrial';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_val_ind, 'asesor', NOW() - INTERVAL 30 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Seis Sigma y Calidad Industrial';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ind, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Manufactura Esbelta & Kaizen';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_val_ind, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Manufactura Esbelta & Kaizen';

-- Mecatrónica
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mec, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Programación PLC y Robótica';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_car_mec, 'asesor', NOW() - INTERVAL 30 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Programación PLC y Robótica';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mec, 'miembro', NOW() - INTERVAL 18 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Sistemas Embebidos e IoT Industrial';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_car_mec, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Sistemas Embebidos e IoT Industrial';

-- Administración
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_adm, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Seminario de Finanzas Corporativas y DCF';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_pao_adm, 'asesor', NOW() - INTERVAL 25 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Seminario de Finanzas Corporativas y DCF';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_adm, 'miembro', NOW() - INTERVAL 12 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Incubadora de Modelos de Negocio y Startups';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_pao_adm, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Incubadora de Modelos de Negocio y Startups';

-- Comercio Internacional
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_com, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Taller de Aduanas y Comercio Exterior T-MEC';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_die_com, 'asesor', NOW() - INTERVAL 25 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Taller de Aduanas y Comercio Exterior T-MEC';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_com, 'miembro', NOW() - INTERVAL 15 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Logística Internacional y Cadena de Suministro';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_die_com, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Logística Internacional y Cadena de Suministro';

-- Mercadotecnia
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mkt, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Growth Marketing & Estrategia Digital';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ren_mkt, 'asesor', NOW() - INTERVAL 25 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Growth Marketing & Estrategia Digital';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mkt, 'miembro', NOW() - INTERVAL 15 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Branding e Insights del Consumidor';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ren_mkt, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Branding e Insights del Consumidor';

-- Psicología
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_psi, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Club de Casos Clínicos y Psicometría';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_and_psi, 'asesor', NOW() - INTERVAL 25 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Club de Casos Clínicos y Psicometría';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_psi, 'miembro', NOW() - INTERVAL 15 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Psicología Organizacional y NOM-035';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_and_psi, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Psicología Organizacional y NOM-035';

-- Derecho
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_der, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Sociedad de Litigio y Juicios Orales';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_seb_der, 'asesor', NOW() - INTERVAL 25 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Sociedad de Litigio y Juicios Orales';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_der, 'miembro', NOW() - INTERVAL 15 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Amparo y Garantías Constitucionales';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_seb_der, 'lider', NOW() - INTERVAL 35 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Amparo y Garantías Constitucionales';

-- =====================================================================
-- 5. SESIONES DE REPASO PROGRAMADAS (FUTURAS)
-- =====================================================================

-- Limpiar sesiones de prueba previas para evitar duplicidad de títulos
DELETE FROM sesiones_repaso WHERE organizador_id IN (
    @u_soft, @u_gab, @u_ind, @u_val_ind, @u_mec, @u_car_mec,
    @u_adm, @u_pao_adm, @u_com, @u_die_com, @u_mkt, @u_ren_mkt,
    @u_psi, @u_and_psi, @u_der, @u_seb_der
);

INSERT INTO sesiones_repaso (titulo, descripcion, materia, modalidad, ubicacion, fecha, hora_inicio, duracion_min, cupo_max, estado, organizador_id, creado_en) VALUES
-- Software (Carrera 3)
('Diseño de Microservicios y Event-Driven con Apache Kafka', 'Revisión paso a paso de patrones SAGA, Event Sourcing y configuración de tópicos en Spring Boot.', 'Arquitectura de Software', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00', 90, 25, 'ABIERTA', @u_gab, NOW()),
('Resolución en Vivo de Árboles AVL y Grafos Dijkstra', 'Preparación para el examen parcial de algoritmos. Traer IDE con Java 17 configurado.', 'Estructuras de Datos y Algoritmos', 'Presencial', 'Laboratorio de Cómputo 2 - Campus Las Torres', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:30:00', 120, 20, 'ABIERTA', @u_soft, NOW()),

-- Industrial (Carrera 4)
('Taller Práctico de Cartas de Control X-R y Capacidad de Proceso en Minitab', 'Revisaremos ejercicios típicos de examen de calidad con cálculo de Cp, Cpk y límites de control.', 'Control Estadístico de la Calidad', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00', 90, 25, 'ABIERTA', @u_val_ind, NOW()),
('Simulación de Células de Manufactura y Cuellos de Botella', 'Práctica guiada en software de simulación para calcular tiempos de ciclo (Takt Time).', 'Sistemas de Manufactura Esbelta', 'Presencial', 'Laboratorio de Métodos - Campus', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00', 120, 18, 'ABIERTA', @u_ind, NOW()),

-- Mecatrónica (Carrera 5)
('Simulación de Ciclos Neumáticos y Ladder en Fluidsim y TIA Portal', 'Práctica guiada para resolver secuencias electroneumáticas A+ B+ A- B- con temporizadores y contadores.', 'Controladores Lógicos Programables (PLC)', 'Virtual', 'Microsoft Teams', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:00:00', 90, 20, 'ABIERTA', @u_car_mec, NOW()),
('Cinemática Inversa y Trayectorias de Robots Manipuladores', 'Deducción geométrica de matrices Denavit-Hartenberg (DH) y cálculo de ángulos articulares en MATLAB.', 'Robótica Industrial y Cinemática', 'Presencial', 'Taller de Mecatrónica - Mesa 3', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:30:00', 120, 15, 'ABIERTA', @u_mec, NOW()),

-- Administración (Carrera 6)
('Maratón de Valuación de Proyectos: VPN, TIR y Costo Promedio WACC', 'Preparación intensiva para el entregable 2 de finanzas. Traer calculadora financiera o Excel en laptop.', 'Finanzas Corporativas Básicas', 'Presencial', 'Biblioteca Campus - Sala Ejecutiva 4', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:30:00', 120, 20, 'ABIERTA', @u_pao_adm, NOW()),
('Taller de Modelos Canvas y Validación de Hipótesis Lean Startup', 'Revisión y retroalimentación entre pares de propuestas de valor y canales de monetización.', 'Modelos de Negocios y Emprendimiento', 'Virtual', 'Google Meet Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:00:00', 90, 25, 'ABIERTA', @u_adm, NOW()),

-- Comercio Internacional (Carrera 7)
('Mesa Redonda: Clasificación Arancelaria y Reglas Generales de la LIGIE', 'Aplicaremos las 6 Reglas Generales de la LIGIE a casos reales de partes automotrices, químicos y textiles.', 'Clasificación Arancelaria y Merceología', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00', 90, 30, 'ABIERTA', @u_die_com, NOW()),
('Auditoría Documental de Pedimentos y Rutas Intermodales México-USA', 'Análisis de pedimentos A1, V1 e IN con cálculo de contribuciones (IGI, DTA e IVA) e Incoterms 2020.', 'Logística Internacional y Fletes Multimodales', 'Presencial', 'Aula Magna Negocios', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:30:00', 120, 25, 'ABIERTA', @u_com, NOW()),

-- Mercadotecnia (Carrera 8)
('Workshop: Configuración del Píxel de Meta, API de Conversiones y GA4', 'Paso a paso para integrar eventos de comercio electrónico (Purchase, AddToCart) y reportes de atribución.', 'Marketing Digital y Redes Sociales', 'Virtual', 'Google Meet', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 90, 25, 'ABIERTA', @u_ren_mkt, NOW()),
('Construcción de Arquitectura de Marca y Manual de Identidad', 'Cómo diseñar el Brand Book: tono de comunicación, arquetipos de personalidad y lineamientos de diseño.', 'Gestión de Marca y Branding Estratégico', 'Presencial', 'Laboratorio de Creatividad y Medios', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:00:00', 120, 20, 'ABIERTA', @u_mkt, NOW()),

-- Psicología (Carrera 9)
('Análisis de Casos Clínicos con Criterios Diagnósticos DSM-5-TR', 'Discusión diagnóstica de 3 casos clínicos reales de trastornos del estado de ánimo y ansiedad generalizada.', 'Psicopatología General', 'Virtual', 'Teams Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:30:00', 90, 20, 'ABIERTA', @u_and_psi, NOW()),
('Taller de Aplicación e Interpretación del Cuestionario NOM-035', 'Metodología para calificar factores de riesgo psicosocial en centros de trabajo y emitir recomendaciones.', 'Psicología Organizacional y del Trabajo', 'Presencial', 'Cámara Gesell - Sala de Observación', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:00:00', 120, 15, 'ABIERTA', @u_psi, NOW()),

-- Derecho (Carrera 10)
('Simulación de Audiencia Inicial y Formulación de Imputación Oral', 'Práctica en vivo de litigio oral penal con roles asignados de fiscal, defensa particular y juez de control.', 'Derecho Procesal Penal y Juicios Orales', 'Presencial', 'Sala de Juicios Orales del Campus', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:00:00', 120, 20, 'ABIERTA', @u_seb_der, NOW()),
('Taller de Redacción de Conceptos de Violación en Juicio de Amparo', 'Estructura lógica de silogismo jurídico para acreditar transgresión a los artículos 14 y 16 constitucionales.', 'Juicio de Amparo y Garantías Constitucionales', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:30:00', 90, 25, 'ABIERTA', @u_der, NOW());

-- =====================================================================
-- 6. INSCRIPCIONES A SESIONES (Para mostrar cupos y participantes)
-- =====================================================================

-- Inscripciones Software
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_soft, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Apache Kafka%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_daniel, NOW() - INTERVAL 2 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Apache Kafka%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_daniel, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Árboles AVL%';

-- Inscripciones Industrial
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_ind, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Minitab%';

-- Inscripciones Mecatrónica
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_mec, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Fluidsim%';

-- Inscripciones Administración
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_adm, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Valuación de Proyectos%';

-- Inscripciones Comercio
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_com, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Clasificación Arancelaria%';

-- Inscripciones Mercadotecnia
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_mkt, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Píxel de Meta%';

-- Inscripciones Psicología
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_psi, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Criterios Diagnósticos%';

-- Inscripciones Derecho
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_der, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Audiencia Inicial%';

-- =====================================================================
-- 7. RESEÑAS COMPLETAS DE SEMESTRE EMPRESARIAL (TODAS LAS CARRERAS)
-- =====================================================================

-- Limpiar reseñas anteriores de estos estudiantes demo para refrescar contenido
DELETE FROM resenas_empresarial WHERE id_estudiante IN (
    @e_soft, @e_gab, @e_ind, @e_val_ind, @e_mec, @e_car_mec,
    @e_adm, @e_pao_adm, @e_com, @e_die_com, @e_mkt, @e_ren_mkt,
    @e_psi, @e_and_psi, @e_der, @e_seb_der
);

INSERT INTO resenas_empresarial (id_estudiante, id_empresa, calificacion, proyecto_desarrollado, aprendizajes, recomendaciones, fecha_resena) VALUES
-- ----------------------------------------------------
-- SOFTWARE (ISSC)
-- ----------------------------------------------------
(@e_soft, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IBM' LIMIT 1), 5,
'Dashboard de monitoreo distribuido y alertas de microservicios en IBM Cloud Kubernetes Service.',
'Aprendí a instrumentar métricas con Prometheus, trazabilidad con OpenTelemetry y despliegues con Helm charts. La mentoría del equipo de ingeniería fue extraordinaria.',
'Lleguen con bases sólidas de Docker, conceptos de concurrencia y actitud receptiva para code reviews estrictos.', NOW() - INTERVAL 45 DAY),

(@e_gab, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Softtek' LIMIT 1), 5,
'Modernización de portal bancario migrando monolito legado a arquitectura de microfrontends con Angular y Spring Boot.',
'Profundicé en patrones de resiliencia (Circuit Breaker, Rate Limiting), contratos de API con OpenAPI y testing de integración.',
'Dominen el control de versiones con Git (flujo Gitflow) y practiquen la redacción de pruebas unitarias.', NOW() - INTERVAL 30 DAY),

(@e_gab, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Tata Consultancy Services (TCS)' LIMIT 1), 4,
'Automatización de pruebas de carga y rendimiento de pasarelas de pago usando JMeter y pipelines en Azure DevOps.',
'Comprendí el ciclo de vida continuo de software en proyectos multinacionales y la importancia de la observabilidad en producción.',
'El nivel de inglés conversacional es indispensable para los stand-ups diarios con equipos en Estados Unidos e India.', NOW() - INTERVAL 20 DAY),

(@e_soft, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'BBVA México' LIMIT 1), 5,
'Implementación de microservicios para validación antifraude de transferencias interbancarias en tiempo real.',
'Aprendí arquitecturas orientadas a eventos con Kafka, encriptación de datos sensibles y cumplimiento regulatorio bancario.',
'Repasen estándares OWASP de seguridad informática y diseño de bases de datos relacionales normalizadas.', NOW() - INTERVAL 12 DAY),

-- ----------------------------------------------------
-- INDUSTRIAL (INDS)
-- ----------------------------------------------------
(@e_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Ternium' LIMIT 1), 5,
'Optimización del flujo de bobinas de acero y reducción del tiempo de cambio de herramental (SMED) en la línea de laminación en frío.',
'Dominé el mapeo de la cadena de valor (VSM), control de piso en SAP y liderazgo de células Kaizen con operadores en planta.',
'Lleguen con excelente manejo de Excel avanzado y nociones muy claras de seguridad industrial y uso de EPP.', NOW() - INTERVAL 50 DAY),

(@e_val_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'General Motors' LIMIT 1), 5,
'Balanceo de líneas de ensamble y reducción de tiempos muertos en la estación de montaje de motores y transmisiones.',
'Aprendí a aplicar estudios de tiempos y movimientos bajo normas MOST, ergonomía industrial y análisis de causa raíz con 8Ds.',
'Prepárense para trabajar en turnos de planta dinámicos; la proactividad para comunicarse con supervisores es clave.', NOW() - INTERVAL 35 DAY),

(@e_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Robert Bosch' LIMIT 1), 5,
'Implementación de sistema de surtimiento Kanban electrónico para reducir inventario en proceso (WIP) en líneas automotrices.',
'Consolidé principios de manufactura Justo a Tiempo (JIT), cálculo de inventarios de seguridad y auditorías de calidad 5S.',
'Muy recomendada; la empresa tiene programas formales de capacitación continua y oportunidades de contratación al graduarte.', NOW() - INTERVAL 18 DAY),

(@e_val_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Home Depot México' LIMIT 1), 5,
'Rediseño del layout de recepción y estandarización del proceso de cross-docking en el Centro de Distribución regional.',
'Desarrollé destreza en optimización de slotting, cálculo de productividad por hora-hombre y auditorías de inventario cíclico.',
'Aprovechen al máximo las juntas operativas semanales para presentar propuestas basadas en datos cuantitativos.', NOW() - INTERVAL 10 DAY),

-- ----------------------------------------------------
-- MECATRÓNICA (IMTC)
-- ----------------------------------------------------
(@e_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Robert Bosch' LIMIT 1), 5,
'Automatización y puesta a punto de una celda robótica de inspección por visión artificial para tarjetas electrónicas de frenos ABS.',
'Aprendí a calibrar cámaras industriales Cognex, programar PLC Siemens S7-1500 en TIA Portal y protocolos de comunicación Profinet.',
'Repasen bien inglés técnico para las juntas con ingenieros de Alemania y demuestren iniciativa en el código de seguridad.', NOW() - INTERVAL 40 DAY),

(@e_car_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Kia México' LIMIT 1), 5,
'Programación, calibración de trayectorias y mantenimiento predictivo en robots de soldadura por puntos Kuka en el área de carrocerías.',
'Perfeccioné el diagnóstico de fallas en servodrives, sincronización cinemática multieje y redes de comunicación DeviceNet/EtherNet/IP.',
'Es una planta de alta exigencia; tengan disciplina estricta en protocolos Lockout/Tagout (LOTO) y seguridad eléctrica.', NOW() - INTERVAL 25 DAY),

(@e_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Nemak' LIMIT 1), 4,
'Integración de pirómetros ópticos infrarrojos y lazos de control PID para monitoreo térmico en hornos de fundición de monoblocks.',
'Comprendí instrumentación industrial en entornos de alta temperatura, adquisición de señales analógicas y programación SCADA.',
'Traigan bases sólidas de termodinámica aplicada y electrónica de potencia.', NOW() - INTERVAL 15 DAY),

(@e_car_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Teleflex Medical' LIMIT 1), 5,
'Validación de servomotores y sistemas neumáticos de alta precisión para el empaque hermético de catéteres médicos.',
'Aprendí sobre validación de cuartos limpios (Cleanrooms), normativas FDA/ISO 13485 y calibración metrológica de sensores.',
'La documentación técnica debe ser impecable; cada cambio en el PLC requiere justificación de control de cambios.', NOW() - INTERVAL 8 DAY),

-- ----------------------------------------------------
-- ADMINISTRACIÓN (LADM)
-- ----------------------------------------------------
(@e_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'FEMSA' LIMIT 1), 5,
'Reestructuración del proceso de adquisiciones indirectas y análisis de rentabilidad por canal de distribución comercial.',
'Consolidé habilidades de negociación con proveedores nacionales, modelado de costos en Power BI y presentación ejecutiva ante directores.',
'Es una empresa con excelente cultura organizacional; aprovechen las mentorías internas y sean muy proactivos.', NOW() - INTERVAL 48 DAY),

(@e_pao_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Grupo Carso' LIMIT 1), 5,
'Evaluación financiera y análisis de sensibilidad de proyectos de inversión de capital (Capex) en la división comercial.',
'Profundicé en modelación financiera de flujo de efectivo descontado (DCF), costeo basado en actividades (ABC) y proyecciones de balance.',
'Dominar Excel a nivel de macros y formulación financiera avanzada les abrirá muchas puertas en la corporación.', NOW() - INTERVAL 28 DAY),

(@e_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'PepsiCo' LIMIT 1), 4,
'Optimización de presupuesto operativo (Opex) y control de indicadores clave de desempeño (KPIs) en plantas de botanas.',
'Aprendí a conciliar variaciones presupuestales contra real, implementar tableros de control y gestionar compras estratégicas.',
'El ritmo de trabajo en consumo masivo es acelerado; organicen sus tiempos con rigor para cumplir entregables.', NOW() - INTERVAL 14 DAY),

(@e_pao_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Scotiabank' LIMIT 1), 5,
'Análisis y dictaminación de crédito empresarial para pymes del sector manufacturero y de servicios.',
'Adquirí agilidad en análisis cualitativo y cuantitativo de estados financieros auditados, razones de liquidez y solvencia.',
'Repasen a conciencia finanzas corporativas y contabilidad general; las evaluaciones crediticias son minuciosas.', NOW() - INTERVAL 6 DAY),

-- ----------------------------------------------------
-- COMERCIO INTERNACIONAL (LCIN)
-- ----------------------------------------------------
(@e_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Ryder de México' LIMIT 1), 5,
'Diseño de rutas multimodales y auditoría de documentación de pedimentos para cruces fronterizos México - Estados Unidos (Laredo).',
'Entendí a fondo las certificaciones C-TPAT y OEA, manejo de inventarios fiscales en almacén y resolución rápida de incidencias aduanales.',
'Estudien muy bien los Incoterms 2020 y las regulaciones y restricciones no arancelarias (RRNAs); es el día a día en patio.', NOW() - INTERVAL 42 DAY),

(@e_die_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Heineken México' LIMIT 1), 5,
'Gestión operativa de booking y fletes marítimos de contenedores para exportación cervecera a mercados de Europa y Asia.',
'Aprendí a negociar tarifas con navieras, emitir cartas de crédito internacionales y coordinar inspecciones fitosanitarias de exportación.',
'El dominio del inglés de negocios es indispensable para coordinarse con agentes aduanales en puertos de destino.', NOW() - INTERVAL 26 DAY),

(@e_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Danone México' LIMIT 1), 4,
'Seguimiento logístico de importación de materias primas lácteas y empaque especializado con control estricto de cadena de frío.',
'Comprendí la tramitación de permisos sanitarios COFEPRIS, control de tiempos de estadía portuaria y reducción de costos de almacenaje.',
'Tengan comunicación constante y asertiva con los transportistas; cualquier retraso en aduana impacta la vida de anaquel.', NOW() - INTERVAL 16 DAY),

(@e_die_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Grupo Carso' LIMIT 1), 5,
'Determinación de origen y emisión de certificados bajo el T-MEC para insumos metalmecánicos y cables de alta tensión.',
'Perfeccioné el cálculo del Valor de Contenido Regional (VCR) por método de costo neto y valor de transacción.',
'Tengan presente la normativa del Anexo 401 del T-MEC; las auditorías de origen del SAT exigen trazabilidad absoluta.', NOW() - INTERVAL 7 DAY),

-- ----------------------------------------------------
-- MERCADOTECNIA (LMKT)
-- ----------------------------------------------------
(@e_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'PepsiCo' LIMIT 1), 5,
'Ejecución del plan de marketing digital y shopper marketing para el lanzamiento de nueva línea de botanas saludables en autoservicios.',
'Aprendí analítica de panel de hogares Nielsen, gestión de presupuesto en medios digitales y coordinación de activaciones con agencias creativas.',
'Prepárense para un ritmo muy dinámico. Aprendan a justificar cada campaña con números de retorno (ROAS) y participación de mercado.', NOW() - INTERVAL 44 DAY),

(@e_ren_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Danone México' LIMIT 1), 5,
'Investigación cualitativa y cuantitativa de tendencias de consumo saludable para el reposicionamiento de marca de yogures funcionales.',
'Diseñé encuestas, coordiné sesiones de Focus Group online y analicé mapas de posicionamiento perceptual de la competencia.',
'Desarrollen su capacidad de síntesis para presentar hallazgos de mercado en formatos visuales claros e inspiradores.', NOW() - INTERVAL 32 DAY),

(@e_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Price Shoes Corporativo' LIMIT 1), 4,
'Optimización de la tasa de conversión (CRO) y estrategia de email marketing automatizado para plataforma de venta por catálogo.',
'Manejé herramientas de pruebas A/B, segmentación de bases de datos por RFM (Recencia, Frecuencia, Monto) y diseño de landing pages.',
'Enfóquense en el Customer Journey; entender los puntos de fricción de la socia vendedora aumentó un 18% las recompras.', NOW() - INTERVAL 19 DAY),

(@e_ren_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'HubSpot' LIMIT 1), 5,
'Estrategia de Inbound Marketing, creación de contenidos de liderazgo de opinión y configuración de workflows automatizados de nutrición.',
'Dominé la calificación de prospectos MQL y SQL, optimización SEO para términos clave de alta intención y reportes de atribución multitáctil.',
'La cultura de trabajo remota y enfocada a resultados es fantástica; adquieran las certificaciones gratuitas de HubSpot Academy antes de entrar.', NOW() - INTERVAL 9 DAY),

-- ----------------------------------------------------
-- PSICOLOGÍA (LPSI)
-- ----------------------------------------------------
(@e_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IMSS' LIMIT 1), 5,
'Implementación del programa de evaluación de factores de riesgo psicosocial bajo la NOM-035 y talleres de prevención de burnout en personal de salud.',
'Desarrollé destreza en aplicación masiva de cuestionarios psicométricos, contención emocional en crisis e informes directivos de clima laboral.',
'Mantengan siempre la ética y confidencialidad rigurosa de los expedientes. El acompañamiento de los psicólogos titulares fue excelente.', NOW() - INTERVAL 46 DAY),

(@e_and_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Teleflex Medical' LIMIT 1), 5,
'Diseño de programa de bienestar integral, ergonomía cognitiva y evaluación del perfil de competencias en operadores de cuartos limpios.',
'Aprendí técnicas de Assessment Center, entrevistas por competencias bajo metodología STAR y dinámicas de integración de equipos.',
'La psicología laboral en empresas manufactureras globales es un campo fértil; demuestren cómo el bienestar impacta en la retención de talento.', NOW() - INTERVAL 30 DAY),

(@e_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Salud Digna' LIMIT 1), 4,
'Protocolo de orientación y apoyo psicoemocional a pacientes y familiares en áreas de diagnóstico clínico especializado.',
'Reforcé habilidades de escucha activa, comunicación empática de resultados delicados y canalización oportuna a redes de apoyo.',
'Cuiden mucho su autocuidado emocional; aprender a desvincularse sanamente al terminar la jornada es vital para no agotarse.', NOW() - INTERVAL 17 DAY),

(@e_and_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'INEGI' LIMIT 1), 5,
'Revisión y calibración psicométrica de reactivos para encuestas nacionales de percepción de bienestar y cohesión social.',
'Profundicé en análisis factorial exploratorio y confirmatorio en SPSS, índices de fiabilidad Alfa de Cronbach y estandarización muestral.',
'Tengan amor por la estadística aplicada a las ciencias sociales; los datos sólidos son la base de políticas públicas efectivas.', NOW() - INTERVAL 11 DAY),

-- ----------------------------------------------------
-- DERECHO (LDRC)
-- ----------------------------------------------------
(@e_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'BBVA México' LIMIT 1), 5,
'Dictaminación jurídica de contratos de crédito empresarial, análisis de garantías prendarias/hipotecarias y cumplimiento regulatorio bancario.',
'Aprendí a detectar cláusulas leoninas, redactar convenios modificatorios y verificar facultades notariales de representantes legales corporativos.',
'Lleguen con excelente ortografía jurídica, comprensión de la Ley General de Títulos y Operaciones de Crédito y ganas de aprender Fintech.', NOW() - INTERVAL 47 DAY),

(@e_seb_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Seguros MAPFRE' LIMIT 1), 5,
'Seguimiento y mediación de controversias en siniestros de responsabilidad civil, dictaminación de coberturas y contestación de quejas en CONDUSEF.',
'Desarrollé agilidad en interpretación de pólizas de seguro, redacción de convenios de transacción extrajudicial y técnicas de negociación.',
'Estudien a fondo la Ley sobre el Contrato de Seguro; argumentar con fundamento legal exacto resuelve el 90% de las reclamaciones.', NOW() - INTERVAL 31 DAY),

(@e_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Seguros Monterrey New York Life' LIMIT 1), 5,
'Auditoría de cumplimiento regulatorio y prevención de lavado de dinero (PLD/FT) ante lineamientos de la Comisión Nacional de Seguros y Fianzas (CNSF).',
'Comprendí matrices de riesgo normativo, integración de expedientes de debida diligencia de clientes y reportes de operaciones inusuales.',
'El área de Compliance corporativo tiene una alta demanda laboral; especializarse en gobierno corporativo es una gran decisión de carrera.', NOW() - INTERVAL 18 DAY),

(@e_seb_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IMSS' LIMIT 1), 4,
'Elaboración de contestaciones de demanda en juicios laborales y seguimiento a expedientes de amparo en materia de seguridad social.',
'Adquirí práctica forense en tribunales laborales, desahogo de pruebas documentales y periciales, e interposición de recursos procesales.',
'Tengan paciencia con los tiempos judiciales burocráticos y lleven una agenda de términos procesales sumamente estricta.', NOW() - INTERVAL 10 DAY);

-- =====================================================================
-- FIN DEL SCRIPT SEED
-- =====================================================================
