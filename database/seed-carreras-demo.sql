-- =====================================================================
-- seed-carreras-demo.sql
-- Datos demostrativos completos para TODAS las carreras de MAPS Connect:
--
-- Carreras cubiertas:
--   - Carrera 3: Ingeniería en Desarrollo de Software (ISSC)
--   - Carrera 4: Ingeniería Industrial y de Sistemas (INDS)
--   - Carrera 5: Ingeniería en Mecatrónica (IMTC)
--   - Carrera 6: Licenciatura en Administración de Empresas (LADM)
--   - Carrera 7: Licenciatura en Comercio Internacional (LCIN)
--   - Carrera 8: Licenciatura en Mercadotecnia (LMKT)
--   - Carrera 9: Licenciatura en Psicología (LPSI)
--   - Carrera 10: Licenciatura en Derecho (LDRC)
--
-- Incluye para cada carrera:
--   1. Estudiantes y Docentes activos (con credencial DemoMaps2026!)
--   2. Asignación de materias según su plan de estudios
--   3. Dudas en el foro con respuestas aceptadas y verificadas por docentes
--   4. Tips académicos prácticos con votos
--   5. Recursos y apuntes académicos compartidos
--   6. Círculos de estudio y sesiones de repaso agendadas
--   7. Reseñas del Semestre Empresarial en empresas aliadas del sector
--
-- Contraseña global para todos los usuarios creados: DemoMaps2026!
-- Hash BCrypt ($2b$12): $2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy
-- =====================================================================

USE maps_conect;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

SET @pwd_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

-- =====================================================================
-- 1. USUARIOS Y ESTUDIANTES DEMO POR CARRERA
-- =====================================================================

-- Carrera 4: Industrial
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mateo Morales Vaca', 'al.industrial@tecmilenio.mx', @pwd_hash, 'estudiante', 65, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_ind = (SELECT id_usuario FROM usuarios WHERE correo = 'al.industrial@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_ind, 4, 'AL04001001', 7, 'Optimizar sistemas de manufactura sustentable y logística de clase mundial.')
ON DUPLICATE KEY UPDATE id_carrera=4, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_ind = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_ind);

-- Docente Industrial
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtro. Fernando Trejo Alanís', 'docente.industrial@tecmilenio.mx', @pwd_hash, 'profesor', 120, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_ind = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.industrial@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_ind, 'DOC-IND-01', 'Calidad Total, Seis Sigma y Manufactura Esbelta', 'Black Belt Lean Six Sigma con experiencia en plantas automotrices y de acero.', 'Martes y Jueves 15:00 - 17:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_ind = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_ind);

-- Carrera 5: Mecatrónica
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Valeria Rivas Del Toro', 'al.mecatronica@tecmilenio.mx', @pwd_hash, 'estudiante', 75, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_mec = (SELECT id_usuario FROM usuarios WHERE correo = 'al.mecatronica@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_mec, 5, 'AL05001001', 7, 'Diseñar robots colaborativos e innovar en automatización médica e industrial.')
ON DUPLICATE KEY UPDATE id_carrera=5, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_mec = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_mec);

-- Docente Mecatrónica
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dr. Alberto Cárdenas Lugo', 'docente.mecatronica@tecmilenio.mx', @pwd_hash, 'profesor', 130, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_mec = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.mecatronica@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_mec, 'DOC-MEC-01', 'Robótica Industrial, PLC y Control Predictivo', 'Investigador en sistemas de control y automatización de líneas de ensamble.', 'Lunes y Viernes 11:00 - 13:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_mec = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_mec);

-- Carrera 6: Administración
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Santiago Peña Montemayor', 'al.administracion@tecmilenio.mx', @pwd_hash, 'estudiante', 55, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_adm = (SELECT id_usuario FROM usuarios WHERE correo = 'al.administracion@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_adm, 6, 'AL06001001', 8, 'Liderar empresas de alto impacto social mediante modelos de negocio éticos.')
ON DUPLICATE KEY UPDATE id_carrera=6, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_adm = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_adm);

-- Docente Administración
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dra. Mónica Esquivel Parra', 'docente.negocios@tecmilenio.mx', @pwd_hash, 'profesor', 115, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_adm = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.negocios@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_adm, 'DOC-ADM-01', 'Finanzas Corporativas y Planeación Estratégica', 'Consultora de finanzas ejecutivas y valuación de proyectos de inversión.', 'Miércoles y Jueves 16:00 - 18:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_adm = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_adm);

-- Carrera 7: Comercio Internacional
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Camila Fuentes Zúñiga', 'al.comercio@tecmilenio.mx', @pwd_hash, 'estudiante', 60, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_com = (SELECT id_usuario FROM usuarios WHERE correo = 'al.comercio@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_com, 7, 'AL07001001', 7, 'Conectar cadenas de suministro globales optimizando operaciones aduaneras.')
ON DUPLICATE KEY UPDATE id_carrera=7, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_com = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_com);

-- Docente Comercio
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtro. Luis Gerardo Ramos', 'docente.comercio@tecmilenio.mx', @pwd_hash, 'profesor', 105, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_com = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.comercio@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_com, 'DOC-COM-01', 'Legislación Aduanera, T-MEC e IMMEX', 'Ex-agente aduanal y asesor en tratados comerciales internacionales.', 'Lunes y Miércoles 17:00 - 19:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_com = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_com);

-- Carrera 8: Mercadotecnia
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Rodrigo Lozano Cavazos', 'al.mercadotecnia@tecmilenio.mx', @pwd_hash, 'estudiante', 70, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_mkt = (SELECT id_usuario FROM usuarios WHERE correo = 'al.mercadotecnia@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_mkt, 8, 'AL08001001', 8, 'Crear estrategias de marketing digital impulsadas por datos y storytelling.')
ON DUPLICATE KEY UPDATE id_carrera=8, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_mkt = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_mkt);

-- Docente Mercadotecnia
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Mtra. Sofía Altamirano Cruz', 'docente.mercadotecnia@tecmilenio.mx', @pwd_hash, 'profesor', 110, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_mkt = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.mercadotecnia@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_mkt, 'DOC-MKT-01', 'Marketing Digital, Analítica Web y CX', 'Especialista en Growth Hacking, embudos de conversión y estrategia de marca.', 'Martes y Jueves 10:00 - 12:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_mkt = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_mkt);

-- Carrera 9: Psicología
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Jimena Garza Villarreal', 'al.psicologia@tecmilenio.mx', @pwd_hash, 'estudiante', 80, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_psi = (SELECT id_usuario FROM usuarios WHERE correo = 'al.psicologia@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_psi, 9, 'AL09001001', 7, 'Promover la salud mental comunitaria y el bienestar psicológico positivo.')
ON DUPLICATE KEY UPDATE id_carrera=9, semestre_actual=7, proposito_vida=VALUES(proposito_vida);
SET @e_psi = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_psi);

-- Docente Psicología
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Dra. Elena Ballesteros Prado', 'docente.psicologia@tecmilenio.mx', @pwd_hash, 'profesor', 125, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_psi = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.psicologia@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_psi, 'DOC-PSI-01', 'Psicometría, Evaluación Clínica y Psicopatología', 'Doctora en psicología con 15 años en docencia clínica y pruebas diagnósticas.', 'Lunes y Jueves 16:00 - 18:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_psi = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_psi);

-- Carrera 10: Derecho
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Emilio Navarro Sotomayor', 'al.derecho@tecmilenio.mx', @pwd_hash, 'estudiante', 85, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_der = (SELECT id_usuario FROM usuarios WHERE correo = 'al.derecho@tecmilenio.mx');

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@u_der, 10, 'AL10001001', 8, 'Defender los derechos fundamentales y la justicia a través del litigio constitucional.')
ON DUPLICATE KEY UPDATE id_carrera=10, semestre_actual=8, proposito_vida=VALUES(proposito_vida);
SET @e_der = (SELECT id_estudiante FROM estudiantes WHERE id_usuario = @u_der);

-- Docente Derecho
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Lic. Javier Montes Canales', 'docente.derecho@tecmilenio.mx', @pwd_hash, 'profesor', 140, 1)
ON DUPLICATE KEY UPDATE nombre_completo=VALUES(nombre_completo), activo=1;
SET @u_doc_der = (SELECT id_usuario FROM usuarios WHERE correo = 'docente.derecho@tecmilenio.mx');

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, horario_asesorias, disponible_chat)
VALUES (@u_doc_der, 'DOC-DER-01', 'Derecho Constitucional, Amparo y Juicios Orales', 'Litigante en materia de amparo y derecho administrativo con amplia trayectoria judicial.', 'Miércoles y Viernes 12:00 - 14:00 hrs', 1)
ON DUPLICATE KEY UPDATE area_especialidad=VALUES(area_especialidad), biografia=VALUES(biografia), disponible_chat=1;
SET @p_doc_der = (SELECT id_profesor FROM profesores WHERE id_usuario = @u_doc_der);

-- =====================================================================
-- 2. MATERIAS ASIGNADAS A LOS ESTUDIANTES DEMO
-- =====================================================================

-- Estudiante Industrial (Semestre 5): Materias 44 (Calidad), 47 (PCP), 48 (Seguridad Industrial), 40 (Probabilidad)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_ind, 40, NOW() - INTERVAL 90 DAY),
(@e_ind, 44, NOW() - INTERVAL 90 DAY),
(@e_ind, 47, NOW() - INTERVAL 90 DAY),
(@e_ind, 48, NOW() - INTERVAL 90 DAY),
(@e_ind, 53, NOW() - INTERVAL 90 DAY);

-- Estudiante Mecatrónica (Semestre 6): Materias 71 (Microcontroladores), 72 (PLC), 73 (Control), 74 (Semestre Empresarial)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_mec, 68, NOW() - INTERVAL 90 DAY),
(@e_mec, 71, NOW() - INTERVAL 90 DAY),
(@e_mec, 72, NOW() - INTERVAL 90 DAY),
(@e_mec, 73, NOW() - INTERVAL 90 DAY),
(@e_mec, 76, NOW() - INTERVAL 90 DAY);

-- Estudiante Administración (Semestre 4): Materias 89 (Costos), 94 (Mat. Financieras), 96 (Talento Humano), 97 (Finanzas Corp)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_adm, 85, NOW() - INTERVAL 90 DAY),
(@e_adm, 89, NOW() - INTERVAL 90 DAY),
(@e_adm, 94, NOW() - INTERVAL 90 DAY),
(@e_adm, 96, NOW() - INTERVAL 90 DAY),
(@e_adm, 97, NOW() - INTERVAL 90 DAY);

-- Estudiante Comercio (Semestre 5): Materias 113 (Derecho aduanero), 117 (Clasificación arancelaria), 119 (T-MEC), 120 (Logística int)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_com, 113, NOW() - INTERVAL 90 DAY),
(@e_com, 117, NOW() - INTERVAL 90 DAY),
(@e_com, 118, NOW() - INTERVAL 90 DAY),
(@e_com, 119, NOW() - INTERVAL 90 DAY),
(@e_com, 120, NOW() - INTERVAL 90 DAY);

-- Estudiante Mercadotecnia (Semestre 6): Materias 139 (Branding), 140 (Marketing digital), 142 (Analítica KPIs), 147 (E-Commerce)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_mkt, 136, NOW() - INTERVAL 90 DAY),
(@e_mkt, 139, NOW() - INTERVAL 90 DAY),
(@e_mkt, 140, NOW() - INTERVAL 90 DAY),
(@e_mkt, 142, NOW() - INTERVAL 90 DAY),
(@e_mkt, 147, NOW() - INTERVAL 90 DAY);

-- Estudiante Psicología (Semestre 5): Materias 160 (Cognitivos), 162 (Psicometría), 163 (Psicopatología), 164 (Entrevista)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_psi, 159, NOW() - INTERVAL 90 DAY),
(@e_psi, 160, NOW() - INTERVAL 90 DAY),
(@e_psi, 162, NOW() - INTERVAL 90 DAY),
(@e_psi, 163, NOW() - INTERVAL 90 DAY),
(@e_psi, 164, NOW() - INTERVAL 90 DAY);

-- Estudiante Derecho (Semestre 6): Materias 184 (Penal), 186 (Obligaciones), 187 (Juicios Orales), 189 (Amparo I)
INSERT IGNORE INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
(@e_der, 183, NOW() - INTERVAL 90 DAY),
(@e_der, 184, NOW() - INTERVAL 90 DAY),
(@e_der, 186, NOW() - INTERVAL 90 DAY),
(@e_der, 187, NOW() - INTERVAL 90 DAY),
(@e_der, 189, NOW() - INTERVAL 90 DAY);

-- Profesores - Materias Asignadas
INSERT IGNORE INTO profesores_materias (id_profesor, id_materia, ciclo_academico) VALUES
(@p_doc_ind, 44, '2026-1'), -- Calidad
(@p_doc_ind, 47, '2026-1'), -- PCP
(@p_doc_mec, 72, '2026-1'), -- PLC
(@p_doc_mec, 76, '2026-1'), -- Robótica
(@p_doc_adm, 97, '2026-1'), -- Finanzas Corp
(@p_doc_adm, 101, '2026-1'), -- Planeación estratégica
(@p_doc_com, 113, '2026-1'), -- Derecho Aduanero
(@p_doc_com, 119, '2026-1'), -- T-MEC
(@p_doc_mkt, 140, '2026-1'), -- Marketing Digital
(@p_doc_mkt, 142, '2026-1'), -- Analítica Web
(@p_doc_psi, 162, '2026-1'), -- Psicometría
(@p_doc_psi, 163, '2026-1'), -- Psicopatología
(@p_doc_der, 187, '2026-1'), -- Juicios Orales
(@p_doc_der, 189, '2026-1'); -- Juicio de Amparo

-- =====================================================================
-- 3. DUDAS EN EL FORO Y RESPUESTAS (POR CARRERA)
-- =====================================================================

-- Industrial: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_ind, 4, 44, '¿Cómo interpretar cuando el Cpk es menor a 1.33 en cartas X-barra?',
'Hola a todos. En la práctica de control estadístico estamos analizando el diámetro de pernos con subgrupos de n=5. Obtuve un Cp de 1.45 pero un Cpk de 1.10. ¿Significa que el proceso está descentrado aunque la dispersión sea aceptable? ¿Cómo se justifica en el entregable?',
NOW() - INTERVAL 3 DAY, 'resuelta');
SET @pub_ind1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_ind1, @u_doc_ind, '¡Correcto Mateo! Al tener Cp > 1.33 tu proceso tiene la capacidad potencial de cumplir especificaciones (baja variabilidad), pero el Cpk de 1.10 indica que la media del proceso está desplazada hacia uno de los límites (LSE o LIE). En tu reporte debes proponer el ajuste del centrado de la máquina.', NOW() - INTERVAL 2 DAY, 1, 1);
SET @resp_ind1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_ind1 WHERE id_publicacion = @pub_ind1;

-- Industrial: Duda 2
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_ind, 4, 47, 'Diferencia en MRP entre Lead Time fijo vs variable en planeación',
'¿Alguien tiene clara la regla para calcular la fecha de liberación de orden de compra si el proveedor tiene un tiempo de entrega de 3 semanas con lote mínimo de 500 piezas?',
NOW() - INTERVAL 1 DAY, 'abierta');

-- Mecatrónica: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_mec, 5, 72, 'Configuración de temporizador TON vs TOF en TIA Portal Siemens S7-1200',
'Estoy programando el ciclo de llenado y sellado de botellas en diagrama de contactos (Ladder). Necesito que la banda continúe 4 segundos después de que el sensor óptico deja de detectar el envase. ¿Uso un bloque TOF directamente sobre la salida de motor?',
NOW() - INTERVAL 4 DAY, 'resuelta');
SET @pub_mec1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_mec1, @u_doc_mec, 'Sí Valeria, el temporizador TOF (Time-Off Delay) es el adecuado: mantiene su salida Q activa durante el tiempo PT fijado una vez que la entrada IN pasa de 1 a 0 lógico. Asegúrate de ligarle un Data Block (DB) exclusivo de instancia para que no colisione con el ton del sellador.', NOW() - INTERVAL 3 DAY, 1, 1);
SET @resp_mec1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_mec1 WHERE id_publicacion = @pub_mec1;

-- Mecatrónica: Duda 2
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_mec, 5, 76, 'Parámetros Denavit-Hartenberg para brazo robótico antropomórfico',
'Tengo duda en la asignación del eje Z en la articulación del codo cuando los ejes de rotación son paralelos. ¿El parámetro theta toma como referencia el eje X anterior?',
NOW() - INTERVAL 12 HOUR, 'abierta');

-- Administración: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_adm, 6, 97, '¿Cómo ponderar el costo de la deuda (Kd) después de impuestos en el cálculo de WACC?',
'Para el caso corporativo de Bimbo, la tasa libre de riesgo es 10.5% (Cetes) y la empresa tiene deuda bancaria al 12% anual con tasa de ISR del 30%. ¿El costo de la deuda neta que entra a la fórmula del WACC es 12% * (1 - 0.30) = 8.4%?',
NOW() - INTERVAL 3 DAY, 'resuelta');
SET @pub_adm1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_adm1, @u_doc_adm, 'Exactamente Santiago. Dado que los intereses financieros son deducibles de impuestos en la legislación mexicana, la deuda tiene un escudo fiscal (Tax Shield), por lo que el costo financiero efectivo es Kd * (1 - T) = 8.4%. Muy buen cálculo.', NOW() - INTERVAL 2 DAY, 1, 1);
SET @resp_adm1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_adm1 WHERE id_publicacion = @pub_adm1;

-- Comercio Internacional: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_com, 7, 119, 'Criterio de salto arancelario vs Valor de Contenido Regional (VCR) en T-MEC',
'Si importamos componentes electrónicos de Asia (partida 8542) para ensamblar sensores automotrices en Guadalajara y exportar a EE.UU., ¿es suficiente el cambio de partida arancelaria o la regla específica exige además el 62.5% de VCR?',
NOW() - INTERVAL 4 DAY, 'resuelta');
SET @pub_com1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_com1, @u_doc_com, 'Excelente pregunta Camila. En el capítulo 87 y sector automotriz del T-MEC las reglas son acumulativas: necesitas el salto arancelario del insumo no originario y cumplir el VCR bajo método de costo neto. Revisa el Anexo 4-B del tratado para la partida exacta.', NOW() - INTERVAL 3 DAY, 1, 1);
SET @resp_com1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_com1 WHERE id_publicacion = @pub_com1;

-- Mercadotecnia: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_mkt, 8, 142, '¿Cómo medir el Retorno del Gasto Publicitario (ROAS) considerando atribución asistida?',
'En nuestra campaña de e-commerce tenemos pauta en TikTok Ads y Meta Ads, pero la compra final ocurre en Google Search orgánico. ¿Qué modelo de atribución recomiendan en GA4 para no quitarle mérito a las campañas de descubrimiento inicial?',
NOW() - INTERVAL 2 DAY, 'resuelta');
SET @pub_mkt1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_mkt1, @u_doc_mkt, 'Rodrigo, te recomiendo configurar el modelo de atribución basada en datos (Data-Driven Attribution) en GA4. A diferencia de Último Clic, este modelo pondera los puntos de contacto iniciales y de consideración mediante algoritmos de Shapley.', NOW() - INTERVAL 1 DAY, 1, 1);
SET @resp_mkt1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_mkt1 WHERE id_publicacion = @pub_mkt1;

-- Psicología: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_psi, 9, 163, 'Diferenciación diagnóstica entre Trastorno de Pánico y Fobia Específica en DSM-5-TR',
'Para la entrega del caso clínico simulado: cuando las crisis de angustia se presentan de manera inesperada (sin estímulo fóbico identificable), ¿se descarta de inmediato la fobia específica aun si el paciente manifiesta temor a salir?',
NOW() - INTERVAL 3 DAY, 'resuelta');
SET @pub_psi1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_psi1, @u_doc_psi, 'Así es Jimena. El criterio A del Trastorno de Pánico requiere ataques de pánico imprevistos y recurrentes. Si el miedo a salir es secundario al temor a sufrir otra crisis donde no haya ayuda disponible, debes evaluar comorbilidad con Agorafobia.', NOW() - INTERVAL 2 DAY, 1, 1);
SET @resp_psi1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_psi1 WHERE id_publicacion = @pub_psi1;

-- Derecho: Duda 1
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado)
VALUES (@e_der, 10, 189, 'Plazo y procedencia del Amparo Indirecto contra Auto de Vinculación a Proceso',
'Compañeros y profesores: frente a un auto de vinculación dictado en audiencia inicial, ¿el plazo es de 15 días hábiles contados a partir de que surte efectos la notificación o de la audiencia oral? ¿Se debe agotar apelación ordinaria?',
NOW() - INTERVAL 4 DAY, 'resuelta');
SET @pub_der1 = LAST_INSERT_ID();

INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion)
VALUES (@pub_der1, @u_doc_der, 'Estimado Emilio: por jurisprudencia obligatoria de la SCJN (art. 107 fracción XII Ley de Amparo), contra el auto de vinculación procede el amparo indirecto sin necesidad de agotar el recurso de apelación previa (excepción al principio de definitividad), dentro de los 15 días hábiles siguientes.', NOW() - INTERVAL 3 DAY, 1, 1);
SET @resp_der1 = LAST_INSERT_ID();
UPDATE publicaciones SET id_respuesta_aceptada = @resp_der1 WHERE id_publicacion = @pub_der1;

-- =====================================================================
-- 4. TIPS ACADÉMICOS CON VOTACIÓN POR CARRERA
-- =====================================================================

-- Industrial
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_ind, 44, 'Consejo Calidad: En los exámenes de Seis Sigma, si no te indican el valor de alfa usa siempre 0.05 (nivel de confianza del 95%). Te ahorra tiempo de deducción en pruebas de hipótesis.', NOW() - INTERVAL 5 DAY, 14),
(@u_ind, 47, 'Tip PCP: Utiliza la regla de Johnson para secuenciar 2 máquinas en serie. Siempre ordena primero los trabajos con menor tiempo en M1 y al final los de menor tiempo en M2.', NOW() - INTERVAL 3 DAY, 9);

-- Mecatrónica
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_mec, 72, 'Tip PLC: En sistemas de parada de emergencia, programa siempre el contacto normalmente cerrado (NC) en físico y abierto en Ladder (Fail-Safe). Nunca confíes solo en lógica por software.', NOW() - INTERVAL 6 DAY, 18),
(@u_mec, 68, 'Truco Electrónica: Si tu transistor BJT se calienta demasiado en conmutación, revisa que esté entrando en saturación profunda forzando una corriente de base Ib = Ic / 10.', NOW() - INTERVAL 2 DAY, 11);

-- Administración
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_adm, 97, 'Estrategia Finanzas: Para calcular el VPN en Excel usa la función =VNA(tasa, flujos_1_al_n) + inversion_inicial (sumando la inversión con su signo negativo, ¡no la incluyas dentro de la fórmula VNA!).', NOW() - INTERVAL 4 DAY, 16),
(@u_adm, 89, 'Tip Costos: En costeo ABC, agrupa actividades por inductores (cost drivers) similares para no saturar la matriz de prorrateo secundario en el proyecto final.', NOW() - INTERVAL 1 DAY, 8);

-- Comercio Internacional
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_com, 113, 'Clave Aduanas: Recuerda que en México el DTA (Derecho de Trámite Aduanero) es del 8 al millar sobre el valor en aduana para importaciones definitivas, salvo exenciones expresas de tratados.', NOW() - INTERVAL 5 DAY, 13),
(@u_com, 120, 'Tip Incoterms 2020: En compras marítimas CFR y CIF, la transmisión del riesgo ocurre cuando la mercancía se carga a bordo del buque, NO cuando llega al puerto de destino.', NOW() - INTERVAL 2 DAY, 10);

-- Mercadotecnia
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_mkt, 140, 'Regla de Oro en Pauta: No modifiques presupuestos de campañas en Meta Ads más del 20% al día; de lo contrario reiniciarás la fase de aprendizaje del algoritmo y subirá tu CPA.', NOW() - INTERVAL 5 DAY, 15),
(@u_mkt, 147, 'Tip E-Commerce: El pop-up de captura de email en checkout recupera hasta un 18% de carritos abandonados si ofreces envío gratis en la primera compra.', NOW() - INTERVAL 2 DAY, 12);

-- Psicología
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_psi, 162, 'Tip Psicometría: Para considerar una escala confiable con Alfa de Cronbach, el valor mínimo aceptable en investigación es 0.70, pero para diagnóstico clínico individual exige 0.85 o más.', NOW() - INTERVAL 6 DAY, 17),
(@u_psi, 164, 'Consejo Entrevista: Las preguntas abiertas con «¿Cómo describirías...?» generan el triple de empatía y apertura que las preguntas que inician con «¿Por qué...?».', NOW() - INTERVAL 3 DAY, 11);

-- Derecho
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
(@u_doc_der, 189, 'Tip de Litigio: En la demanda de amparo, estructura cada concepto de violación demostrando el silogismo: Premisa mayor (derecho humano vulnerado), Premisa menor (acto reclamado de la autoridad) y Conclusión.', NOW() - INTERVAL 5 DAY, 20),
(@u_der, 187, 'Tip Juicios Orales: En el contrainterrogatorio formula únicamente preguntas sugestivas y de un solo hecho. Nunca hagas la pregunta de más ni preguntes el «¿por qué?».', NOW() - INTERVAL 1 DAY, 15);

-- =====================================================================
-- 5. RECURSOS ACADÉMICOS Y APUNTES COMPARTIDOS POR CARRERA
-- =====================================================================

-- Industrial
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_ind, 44, 'Plantilla de Análisis de Capacidad del Proceso (Cp, Cpk, Pp, Ppk).xlsx',
'Plantilla en Excel formulada para ingresar muestras de 5 elementos, calcular límites de control automático y generar gráficos de campana normal.',
'https://tecmilenio.instructure.com/resources/industrial-control-calidad.xlsx', 'XLS', NOW() - INTERVAL 10 DAY, 42, 0, 0),
(@u_doc_ind, 47, 'Guía Rápida de Balanceo de Líneas y Cálculo de Takt Time.pdf',
'Compendio con las fórmulas de tiempo ciclo, número teórico de estaciones de trabajo y porcentaje de eficiencia de balanceo.',
'https://tecmilenio.instructure.com/resources/takt-time-manufactura.pdf', 'PDF', NOW() - INTERVAL 6 DAY, 58, 0, 0);

-- Mecatrónica
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_mec, 72, 'Compendio de Instrucciones Básicas en Ladder para TIA Portal Siemens.pdf',
'Guía paso a paso con ejemplos de enclavamiento, temporizadores TON/TOF, contadores CTU y flancos positivos.',
'https://tecmilenio.instructure.com/resources/plc-ladder-siemens.pdf', 'PDF', NOW() - INTERVAL 8 DAY, 63, 0, 0),
(@u_doc_mec, 76, 'Formulario de Transformaciones Homogéneas y Cinemática Directa.pdf',
'Resumen de matrices de rotación y traslación en coordenadas homogéneas para manipuladores seriales.',
'https://tecmilenio.instructure.com/resources/cinematica-robotica.pdf', 'PDF', NOW() - INTERVAL 4 DAY, 51, 0, 0);

-- Administración
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_adm, 97, 'Modelo Financiero de Proyección a 5 Años y Valuación por Flujos Descontados.xlsx',
'Plantilla corporativa con Estado de Resultados, Balance General proyectado, cálculo de CAPEX, Capital de Trabajo y WACC.',
'https://tecmilenio.instructure.com/resources/modelo-financiero-dcf.xlsx', 'XLS', NOW() - INTERVAL 9 DAY, 48, 0, 0),
(@u_doc_adm, 101, 'Framework de Balanced Scorecard y Cuadro de Mando Integral.pdf',
'Matriz de indicadores estratégicos organizados en las 4 perspectivas: financiera, clientes, procesos internos y aprendizaje.',
'https://tecmilenio.instructure.com/resources/balanced-scorecard.pdf', 'PDF', NOW() - INTERVAL 5 DAY, 37, 0, 0);

-- Comercio Internacional
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_com, 113, 'Diagrama de Flujo del Despacho Aduanero Mexicano con Sistema Automatizado (SAAI).pdf',
'Infografía legal que explica el proceso desde la prevalidación del pedimento, pago bancario, semáforo fiscal y reconocimiento aduanero.',
'https://tecmilenio.instructure.com/resources/despacho-aduanero-mexico.pdf', 'PDF', NOW() - INTERVAL 7 DAY, 55, 0, 0),
(@u_doc_com, 120, 'Matriz Comparativa Oficial de Incoterms 2020 de la CCI.pdf',
'Tabla resumen que delimita con claridad quién asume gastos, flete principal, seguro marítimo y trámite de importación.',
'https://tecmilenio.instructure.com/resources/incoterms-2020-resumen.pdf', 'PDF', NOW() - INTERVAL 3 DAY, 72, 0, 0);

-- Mercadotecnia
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_mkt, 140, 'Dashboard de Métricas de Rendimiento Digital y KPIs de Pauta.xlsx',
'Reporte automatizado para medir CTR, CPC, CPM, CPA y ROAS comparativo entre Google Ads y Meta Ads.',
'https://tecmilenio.instructure.com/resources/dashboard-marketing-digital.xlsx', 'XLS', NOW() - INTERVAL 6 DAY, 49, 0, 0),
(@u_doc_mkt, 139, 'Plantilla de Construcción de Buyer Persona y Mapa de Empatía.pdf',
'Formato editable para definir dolores, motivaciones, canales de contacto y propuesta de valor de marca.',
'https://tecmilenio.instructure.com/resources/buyer-persona-framework.pdf', 'PDF', NOW() - INTERVAL 2 DAY, 44, 0, 0);

-- Psicología
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_psi, 164, 'Guía de Historia Clínica Psicológica y Examen del Estado Mental.pdf',
'Protocolo completo para entrevista diagnóstica inicial: apariencia, afecto, curso del pensamiento y cognición.',
'https://tecmilenio.instructure.com/resources/historia-clinica-psicologica.pdf', 'PDF', NOW() - INTERVAL 7 DAY, 67, 0, 0),
(@u_doc_psi, 162, 'Fichas Técnicas de Pruebas Psicométricas Estandarizadas (MMPI-2, WISC-V, 16PF).pdf',
'Fichas de referencia rápida con baremos mexicanos, validez, confiabilidad y puntos de corte clínicos.',
'https://tecmilenio.instructure.com/resources/fichas-psicometricas.pdf', 'PDF', NOW() - INTERVAL 4 DAY, 83, 0, 0);

-- Derecho
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
(@u_der, 189, 'Formato Modelo de Demanda de Juicio de Amparo Indirecto en Materia Penal.docx',
'Machote profesional con capítulos de suspensión del acto reclamado, hechos bajo protesta de decir verdad y conceptos de violación.',
'https://tecmilenio.instructure.com/resources/demanda-amparo-penal.docx', 'DOC', NOW() - INTERVAL 8 DAY, 95, 0, 0),
(@u_doc_der, 187, 'Protocolo de Alegatos de Apertura y Clausura en el Sistema Penal Acusatorio.pdf',
'Estructura de teoría del fáctico, probatorio y jurídico con técnicas de persuasión retórica para audiencias orales.',
'https://tecmilenio.instructure.com/resources/guia-litigio-oral.pdf', 'PDF', NOW() - INTERVAL 3 DAY, 88, 0, 0);

-- =====================================================================
-- 6. COMUNIDADES PERMANENTES (CÍRCULOS DE ESTUDIO) Y MIEMBROS
-- =====================================================================

-- Círculos por Carrera (2 por carrera)
-- Software (Carrera 3)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Arquitectura de Software y Algoritmos', @u_soft, 3, 2, 'publica', 'https://tecmilenio.zoom.us/j/c-software-arq')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio Web y Mobile Full-Stack', (SELECT id_usuario FROM usuarios WHERE correo = 'al07080560@tecmilenio.mx'), 3, 4, 'publica', 'https://tecmilenio.zoom.us/j/c-software-web')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Industrial (Carrera 4)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Seis Sigma y Calidad Industrial', @u_ind, 4, 44, 'publica', 'https://tecmilenio.zoom.us/j/c-industrial-calidad')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Manufactura Esbelta & Kaizen', @u_doc_ind, 4, 46, 'publica', 'https://tecmilenio.zoom.us/j/c-industrial-lean')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Mecatrónica (Carrera 5)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio de Programación PLC y Robótica', @u_mec, 5, 72, 'publica', 'https://tecmilenio.zoom.us/j/c-mecatronica-plc')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Sistemas Embebidos e IoT Industrial', @u_doc_mec, 5, 75, 'publica', 'https://tecmilenio.zoom.us/j/c-mecatronica-iot')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Administración (Carrera 6)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Seminario de Finanzas Corporativas y DCF', @u_adm, 6, 97, 'publica', 'https://tecmilenio.zoom.us/j/c-administracion-finanzas')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Incubadora de Modelos de Negocio y Startups', @u_doc_adm, 6, 100, 'publica', 'https://tecmilenio.zoom.us/j/c-administracion-modelos')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Comercio Internacional (Carrera 7)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Taller de Aduanas y Comercio Exterior T-MEC', @u_com, 7, 119, 'publica', 'https://tecmilenio.zoom.us/j/c-comercio-aduanas')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Logística Internacional y Cadena de Suministro', @u_doc_com, 7, 121, 'publica', 'https://tecmilenio.zoom.us/j/c-comercio-logistica')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Mercadotecnia (Carrera 8)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Growth Marketing & Estrategia Digital', @u_mkt, 8, 140, 'publica', 'https://tecmilenio.zoom.us/j/c-mercadotecnia-digital')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Laboratorio de Branding e Insights del Consumidor', @u_doc_mkt, 8, 143, 'publica', 'https://tecmilenio.zoom.us/j/c-mercadotecnia-branding')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Psicología (Carrera 9)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Club de Casos Clínicos y Psicometría', @u_psi, 9, 163, 'publica', 'https://tecmilenio.zoom.us/j/c-psicologia-clinica')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Comunidad de Psicología Organizacional y NOM-035', @u_doc_psi, 9, 166, 'publica', 'https://tecmilenio.zoom.us/j/c-psicologia-organizacional')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Derecho (Carrera 10)
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Sociedad de Litigio y Juicios Orales', @u_der, 10, 187, 'publica', 'https://tecmilenio.zoom.us/j/c-derecho-oralidad')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, privacidad, enlace_sala_virtual)
VALUES ('Círculo de Amparo y Garantías Constitucionales', @u_doc_der, 10, 188, 'publica', 'https://tecmilenio.zoom.us/j/c-derecho-amparo')
ON DUPLICATE KEY UPDATE nombre_comunidad=VALUES(nombre_comunidad);

-- Miembros de las Comunidades (Para poblar "Tus grupos permanentes")
-- Software
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_soft, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Arquitectura de Software y Algoritmos';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, (SELECT id_usuario FROM usuarios WHERE correo = 'al07080560@tecmilenio.mx'), 'miembro', NOW() - INTERVAL 30 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Arquitectura de Software y Algoritmos';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_soft, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio Web y Mobile Full-Stack';

-- Industrial
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ind, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Seis Sigma y Calidad Industrial';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_ind, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Manufactura Esbelta & Kaizen';

-- Mecatrónica
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mec, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Programación PLC y Robótica';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mec, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Sistemas Embebidos e IoT Industrial';

-- Administración
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_adm, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Seminario de Finanzas Corporativas y DCF';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_adm, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Incubadora de Modelos de Negocio y Startups';

-- Comercio
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_com, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Taller de Aduanas y Comercio Exterior T-MEC';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_com, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Logística Internacional y Cadena de Suministro';

-- Mercadotecnia
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mkt, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Growth Marketing & Estrategia Digital';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_mkt, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Laboratorio de Branding e Insights del Consumidor';

-- Psicología
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_psi, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Club de Casos Clínicos y Psicometría';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_psi, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Comunidad de Psicología Organizacional y NOM-035';

-- Derecho
INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_der, 'lider', NOW() - INTERVAL 40 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Sociedad de Litigio y Juicios Orales';

INSERT IGNORE INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union)
SELECT c.id_comunidad, @u_der, 'miembro', NOW() - INTERVAL 20 DAY
FROM comunidades_estudio c WHERE c.nombre_comunidad = 'Círculo de Amparo y Garantías Constitucionales';

-- =====================================================================
-- 7. SESIONES DE REPASO FUTURAS E INSCRIPCIONES
-- =====================================================================

INSERT INTO sesiones_repaso (titulo, descripcion, materia, modalidad, ubicacion, fecha, hora_inicio, duracion_min, cupo_max, estado, organizador_id, creado_en) VALUES
-- Software (Carrera 3)
('Diseño de Microservicios y Event-Driven con Apache Kafka', 'Revisión paso a paso de patrones SAGA, Event Sourcing y configuración de tópicos en Spring Boot.', 'Arquitectura de Software', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00', 90, 25, 'ABIERTA', @u_doc_soft, NOW()),
('Resolución en Vivo de Árboles AVL y Grafos Dijkstra', 'Preparación para el examen parcial de algoritmos. Traer IDE con Java 17 configurado.', 'Estructuras de Datos y Algoritmos', 'Presencial', 'Laboratorio de Cómputo 2 - Campus Las Torres', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '15:30:00', 120, 20, 'ABIERTA', @u_soft, NOW()),

-- Industrial (Carrera 4)
('Taller Práctico de Cartas de Control X-R y Capacidad de Proceso en Minitab', 'Revisaremos ejercicios típicos de examen de calidad con cálculo de Cp, Cpk y límites de control.', 'Control Estadístico de la Calidad', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '17:00:00', 90, 25, 'ABIERTA', @u_doc_ind, NOW()),
('Simulación de Células de Manufactura y Cuellos de Botella', 'Práctica guiada en software de simulación para calcular tiempos de ciclo (Takt Time).', 'Sistemas de Manufactura Esbelta', 'Presencial', 'Laboratorio de Métodos - Campus', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '16:00:00', 120, 18, 'ABIERTA', @u_ind, NOW()),

-- Mecatrónica (Carrera 5)
('Simulación de Ciclos Neumáticos y Ladder en Fluidsim y TIA Portal', 'Práctica guiada para resolver secuencias electroneumáticas A+ B+ A- B- con temporizadores y contadores.', 'Controladores Lógicos Programables (PLC)', 'Virtual', 'Microsoft Teams', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:00:00', 90, 20, 'ABIERTA', @u_doc_mec, NOW()),
('Cinemática Inversa y Trayectorias de Robots Manipuladores', 'Deducción geométrica de matrices Denavit-Hartenberg (DH) y cálculo de ángulos articulares en MATLAB.', 'Robótica Industrial y Cinemática', 'Presencial', 'Taller de Mecatrónica - Mesa 3', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '14:30:00', 120, 15, 'ABIERTA', @u_mec, NOW()),

-- Administración (Carrera 6)
('Maratón de Valuación de Proyectos: VPN, TIR y Costo Promedio WACC', 'Preparación intensiva para el entregable 2 de finanzas. Traer calculadora financiera o Excel en laptop.', 'Finanzas Corporativas Básicas', 'Presencial', 'Biblioteca Campus - Sala Ejecutiva 4', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:30:00', 120, 20, 'ABIERTA', @u_doc_adm, NOW()),
('Taller de Modelos Canvas y Validación de Hipótesis Lean Startup', 'Revisión y retroalimentación entre pares de propuestas de valor y canales de monetización.', 'Modelos de Negocios y Emprendimiento', 'Virtual', 'Google Meet Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:00:00', 90, 25, 'ABIERTA', @u_adm, NOW()),

-- Comercio Internacional (Carrera 7)
('Mesa Redonda: Clasificación Arancelaria y Reglas Generales de la LIGIE', 'Aplicaremos las 6 Reglas Generales de la LIGIE a casos reales de partes automotrices, químicos y textiles.', 'Clasificación Arancelaria y Merceología', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00:00', 90, 30, 'ABIERTA', @u_doc_com, NOW()),
('Auditoría Documental de Pedimentos y Rutas Intermodales México-USA', 'Análisis de pedimentos A1, V1 e IN con cálculo de contribuciones (IGI, DTA e IVA) e Incoterms 2020.', 'Logística Internacional y Fletes Multimodales', 'Presencial', 'Aula Magna Negocios', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '16:30:00', 120, 25, 'ABIERTA', @u_com, NOW()),

-- Mercadotecnia (Carrera 8)
('Workshop: Configuración del Píxel de Meta, API de Conversiones y GA4', 'Paso a paso para integrar eventos de comercio electrónico (Purchase, AddToCart) y reportes de atribución.', 'Marketing Digital y Redes Sociales', 'Virtual', 'Google Meet', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 90, 25, 'ABIERTA', @u_doc_mkt, NOW()),
('Construcción de Arquitectura de Marca y Manual de Identidad', 'Cómo diseñar el Brand Book: tono de comunicación, arquetipos de personalidad y lineamientos de diseño.', 'Gestión de Marca y Branding Estratégico', 'Presencial', 'Laboratorio de Creatividad y Medios', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '15:00:00', 120, 20, 'ABIERTA', @u_mkt, NOW()),

-- Psicología (Carrera 9)
('Análisis de Casos Clínicos con Criterios Diagnósticos DSM-5-TR', 'Discusión diagnóstica de 3 casos clínicos reales de trastornos del estado de ánimo y ansiedad generalizada.', 'Psicopatología General', 'Virtual', 'Teams Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 3 DAY), '16:30:00', 90, 20, 'ABIERTA', @u_doc_psi, NOW()),
('Taller de Aplicación e Interpretación del Cuestionario NOM-035', 'Metodología para calificar factores de riesgo psicosocial en centros de trabajo y emitir recomendaciones.', 'Psicología Organizacional y del Trabajo', 'Presencial', 'Cámara Gesell - Sala de Observación', DATE_ADD(CURDATE(), INTERVAL 6 DAY), '17:00:00', 120, 15, 'ABIERTA', @u_psi, NOW()),

-- Derecho (Carrera 10)
('Simulación de Audiencia Inicial y Formulación de Imputación Oral', 'Práctica en vivo de litigio oral penal con roles asignados de fiscal, defensa particular y juez de control.', 'Derecho Procesal Penal y Juicios Orales', 'Presencial', 'Sala de Juicios Orales del Campus', DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:00:00', 120, 20, 'ABIERTA', @u_doc_der, NOW()),
('Taller de Redacción de Conceptos de Violación en Juicio de Amparo', 'Estructura lógica de silogismo jurídico para acreditar transgresión a los artículos 14 y 16 constitucionales.', 'Juicio de Amparo y Garantías Constitucionales', 'Virtual', 'Zoom Tecmilenio', DATE_ADD(CURDATE(), INTERVAL 5 DAY), '18:30:00', 90, 25, 'ABIERTA', @u_der, NOW());

-- Inscripciones Demo
INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_soft, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Apache Kafka%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, (SELECT id_usuario FROM usuarios WHERE correo = 'al07080560@tecmilenio.mx'), NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Árboles AVL%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_ind, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Minitab%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_mec, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Fluidsim%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_adm, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Valuación de Proyectos%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_com, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Clasificación Arancelaria%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_mkt, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Píxel de Meta%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_psi, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Criterios Diagnósticos%';

INSERT IGNORE INTO sesion_inscripciones (id_sesion, id_usuario, fecha_inscripcion)
SELECT s.id_sesion, @u_der, NOW() - INTERVAL 1 DAY
FROM sesiones_repaso s WHERE s.titulo LIKE '%Audiencia Inicial%';

-- =====================================================================
-- 8. RESEÑAS MULTIDISCIPLINARIAS DE SEMESTRE EMPRESARIAL
-- =====================================================================

INSERT INTO resenas_empresarial (id_estudiante, id_empresa, calificacion, proyecto_desarrollado, aprendizajes, recomendaciones, fecha_resena) VALUES
-- Software
(@e_soft, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IBM' LIMIT 1), 5,
'Dashboard de monitoreo distribuido y alertas de microservicios en IBM Cloud Kubernetes Service.',
'Aprendí a instrumentar métricas con Prometheus, trazabilidad con OpenTelemetry y despliegues con Helm charts. La mentoría del equipo de ingeniería fue extraordinaria.',
'Lleguen con bases sólidas de Docker, conceptos de concurrencia y actitud receptiva para code reviews estrictos.', NOW() - INTERVAL 45 DAY),

(@e_soft, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'BBVA México' LIMIT 1), 5,
'Implementación de microservicios para validación antifraude de transferencias interbancarias en tiempo real.',
'Aprendí arquitecturas orientadas a eventos con Kafka, encriptación de datos sensibles y cumplimiento regulatorio bancario.',
'Repasen estándares OWASP de seguridad informática y diseño de bases de datos relacionales normalizadas.', NOW() - INTERVAL 12 DAY),

-- Industrial
(@e_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Ternium' LIMIT 1), 5,
'Optimización del flujo de bobinas de acero y reducción del tiempo de cambio de herramental (SMED) en la línea de laminación en frío.',
'Dominé el mapeo de la cadena de valor (VSM), control de piso en SAP y liderazgo de células Kaizen con operadores en planta.',
'Lleguen con excelente manejo de Excel avanzado y nociones muy claras de seguridad industrial y uso de EPP.', NOW() - INTERVAL 50 DAY),

(@e_ind, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Robert Bosch' LIMIT 1), 5,
'Implementación de sistema de surtimiento Kanban electrónico para reducir inventario en proceso (WIP) en líneas automotrices.',
'Consolidé principios de manufactura Justo a Tiempo (JIT), cálculo de inventarios de seguridad y auditorías de calidad 5S.',
'Muy recomendada; la empresa tiene programas formales de capacitación continua y oportunidades de contratación al graduarte.', NOW() - INTERVAL 18 DAY),

-- Mecatrónica
(@e_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Robert Bosch' LIMIT 1), 5,
'Automatización y puesta a punto de una celda robótica de inspección por visión artificial para tarjetas electrónicas de frenos ABS.',
'Aprendí a calibrar cámaras industriales Cognex, programar PLC Siemens S7-1500 en TIA Portal y protocolos de comunicación Profinet.',
'Repasen bien inglés técnico para las juntas con ingenieros de Alemania y demuestren iniciativa en el código de seguridad.', NOW() - INTERVAL 40 DAY),

(@e_mec, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Nemak' LIMIT 1), 4,
'Integración de pirómetros ópticos infrarrojos y lazos de control PID para monitoreo térmico en hornos de fundición de monoblocks.',
'Comprendí instrumentación industrial en entornos de alta temperatura, adquisición de señales analógicas y programación SCADA.',
'Traigan bases sólidas de termodinámica aplicada y electrónica de potencia.', NOW() - INTERVAL 15 DAY),

-- Administración
(@e_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'FEMSA' LIMIT 1), 5,
'Reestructuración del proceso de adquisiciones indirectas y análisis de rentabilidad por canal de distribución comercial.',
'Consolidé habilidades de negociación con proveedores nacionales, modelado de costos en Power BI y presentación ejecutiva ante directores.',
'Es una empresa con excelente cultura organizacional; aprovechen las mentorías internas y sean muy proactivos.', NOW() - INTERVAL 48 DAY),

(@e_adm, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'PepsiCo' LIMIT 1), 4,
'Optimización de presupuesto operativo (Opex) y control de indicadores clave de desempeño (KPIs) en plantas de botanas.',
'Aprendí a conciliar variaciones presupuestales contra real, implementar tableros de control y gestionar compras estratégicas.',
'El ritmo de trabajo en consumo masivo es acelerado; organicen sus tiempos con rigor para cumplir entregables.', NOW() - INTERVAL 14 DAY),

-- Comercio Internacional
(@e_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Ryder de México' LIMIT 1), 5,
'Diseño de rutas multimodales y auditoría de documentación de pedimentos para cruces fronterizos México - Estados Unidos (Laredo).',
'Entendí a fondo las certificaciones C-TPAT y OEA, manejo de inventarios fiscales en almacén y resolución rápida de incidencias aduanales.',
'Estudien muy bien los Incoterms 2020 y las regulaciones y restricciones no arancelarias (RRNAs); es el día a día en patio.', NOW() - INTERVAL 42 DAY),

(@e_com, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Danone México' LIMIT 1), 4,
'Seguimiento logístico de importación de materias primas lácteas y empaque especializado con control estricto de cadena de frío.',
'Comprendí la tramitación de permisos sanitarios COFEPRIS, control de tiempos de estadía portuaria y reducción de costos de almacenaje.',
'Tengan comunicación constante y asertiva con los transportistas; cualquier retraso en aduana impacta la vida de anaquel.', NOW() - INTERVAL 16 DAY),

-- Mercadotecnia
(@e_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'PepsiCo' LIMIT 1), 5,
'Ejecución del plan de marketing digital y shopper marketing para el lanzamiento de nueva línea de botanas saludables en autoservicios.',
'Aprendí analítica de panel de hogares Nielsen, gestión de presupuesto en medios digitales y coordinación de activaciones con agencias creativas.',
'Prepárense para un ritmo muy dinámico. Aprendan a justificar cada campaña con números de retorno (ROAS) y participación de mercado.', NOW() - INTERVAL 44 DAY),

(@e_mkt, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Price Shoes Corporativo' LIMIT 1), 4,
'Optimización de la tasa de conversión (CRO) y estrategia de email marketing automatizado para plataforma de venta por catálogo.',
'Manejé herramientas de pruebas A/B, segmentación de bases de datos por RFM (Recencia, Frecuencia, Monto) y diseño de landing pages.',
'Enfóquense en el Customer Journey; entender los puntos de fricción de la socia vendedora aumentó un 18% las recompras.', NOW() - INTERVAL 19 DAY),

-- Psicología
(@e_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IMSS' LIMIT 1), 5,
'Implementación del programa de evaluación de factores de riesgo psicosocial bajo la NOM-035 y talleres de prevención de burnout en personal de salud.',
'Desarrollé destreza en aplicación masiva de cuestionarios psicométricos, contención emocional en crisis e informes directivos de clima laboral.',
'Mantengan siempre la ética y confidencialidad rigurosa de los expedientes. El acompañamiento de los psicólogos titulares fue excelente.', NOW() - INTERVAL 46 DAY),

(@e_psi, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Salud Digna' LIMIT 1), 4,
'Protocolo de orientación y apoyo psicoemocional a pacientes y familiares en áreas de diagnóstico clínico especializado.',
'Reforcé habilidades de escucha activa, comunicación empática de resultados delicados y canalización oportuna a redes de apoyo.',
'Cuiden mucho su autocuidado emocional; aprender a desvincularse sanamente al terminar la jornada es vital para no agotarse.', NOW() - INTERVAL 17 DAY),

-- Derecho
(@e_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'BBVA México' LIMIT 1), 5,
'Dictaminación jurídica de contratos de crédito empresarial, análisis de garantías prendarias/hipotecarias y cumplimiento regulatorio bancario.',
'Aprendí a detectar cláusulas leoninas, redactar convenios modificatorios y verificar facultades notariales de representantes legales corporativos.',
'Lleguen con excelente ortografía jurídica, comprensión de la Ley General de Títulos y Operaciones de Crédito y ganas de aprender Fintech.', NOW() - INTERVAL 47 DAY),

(@e_der, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Seguros Monterrey New York Life' LIMIT 1), 5,
'Auditoría de cumplimiento regulatorio y prevención de lavado de dinero (PLD/FT) ante lineamientos de la Comisión Nacional de Seguros y Fianzas (CNSF).',
'Comprendí matrices de riesgo normativo, integración de expedientes de debida diligencia de clientes y reportes de operaciones inusuales.',
'El área de Compliance corporativo tiene una alta demanda laboral; especializarse en gobierno corporativo es una gran decisión de carrera.', NOW() - INTERVAL 18 DAY);

