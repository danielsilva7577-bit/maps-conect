-- MAPS Connect · Datos de demostración para Mensajes, Certificados y Tips.
-- Población de conversaciones/mensajes y tips académicos para que las
-- pantallas de mensajes y tips muestren contenido real.
-- Script idempotente (se puede re-ejecutar sin duplicar datos).

USE maps_conect;

-- Forzar la collation de la conexión para que coincida con las columnas
-- (para evitar errores de "Illegal mix of collations" al comparar variables).
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ---- Profesor demo (interlocutor de la conversación) ----
-- El RolConverter guarda el rol en minúsculas.
SET @profCorreo = 'profesor.demo@tecmilenio.mx';
SET @profId = (SELECT id_usuario FROM usuarios WHERE correo = @profCorreo);

-- Limpieza de datos demo de este seed
DELETE FROM votos_tips WHERE id_usuario IN (12, @profId);
DELETE FROM tips_academicos WHERE id_usuario IN (12, @profId);
DELETE FROM mensajes WHERE id_conversacion IN (
  SELECT id_conversacion FROM conversaciones
  WHERE (id_usuario_1 = 12 AND id_usuario_2 = @profId)
     OR (id_usuario_1 = @profId AND id_usuario_2 = 12)
);
DELETE FROM conversaciones
  WHERE (id_usuario_1 = 12 AND id_usuario_2 = @profId)
     OR (id_usuario_1 = @profId AND id_usuario_2 = 12);
DELETE FROM profesores WHERE id_usuario = @profId;
DELETE FROM usuarios WHERE correo = @profCorreo;

-- Crear el profesor (idempotente)
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Prof. Carlos Mendoza', @profCorreo,
        '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy', 'profesor', 10, 1)
ON DUPLICATE KEY UPDATE id_usuario = LAST_INSERT_ID(id_usuario), nombre_completo = VALUES(nombre_completo);
SET @profId = LAST_INSERT_ID();

INSERT INTO profesores (id_usuario, numero_nomina, area_especialidad, biografia, disponible_chat)
VALUES (@profId, 'NOM-1001', 'Ingeniería de Software',
        'Profesor de Arquitectura y Diseño de Software.', 1)
ON DUPLICATE KEY UPDATE id_profesor = LAST_INSERT_ID(id_profesor);

-- ---- 1) Conversación estudiante (12) <-> profesor (@profId) ----
-- El trigger exige id_usuario_1 < id_usuario_2
INSERT INTO conversaciones (id_usuario_1, id_usuario_2, fecha_inicio) VALUES (12, @profId, NOW() - INTERVAL 6 DAY);
SET @convId = LAST_INSERT_ID();

INSERT INTO mensajes (id_conversacion, id_emisor, contenido, fecha_envio, leido) VALUES
  (@convId, 12, 'Buenas tardes profesor, ¿podría revisar mi proyecto de arquitectura la próxima semana?', NOW() - INTERVAL 6 DAY, 1),
  (@convId, @profId, 'Claro, agéndame una asesoría el martes a las 4pm y lo revisamos con detalle.', NOW() - INTERVAL 6 DAY, 1),
  (@convId, 12, 'Perfecto, quedo pendiente. Le comparto los diagramas antes de la sesión.', NOW() - INTERVAL 5 DAY, 1),
  (@convId, @profId, 'Genial, recuerda revisar también la sección de despliegue y los patrones de diseño.', NOW() - INTERVAL 5 DAY, 0),
  (@convId, 12, 'Gracias por el recordatorio, voy a actualizar los diagramas de componentes.', NOW() - INTERVAL 1 DAY, 0);

-- ---- 2) Tips académicos ----
-- El trigger de votos_tips incrementa total_votos; por eso los insertamos en 0
-- y luego registramos los votos para que el total quede coherente.
INSERT INTO tips_academicos (id_usuario, id_materia, contenido, fecha_publicacion, total_votos) VALUES
  (@profId, 17, 'Consejo: para los exámenes de arquitectura, dibuja siempre el diagrama de despliegue antes de responder. La mayoría de los puntos se ganan ahí.', NOW() - INTERVAL 3 DAY, 0),
  (@profId, 13, 'Tip: en SQL practica las subconsultas correlacionadas con EXISTS; son las que más pesan en evaluación.', NOW() - INTERVAL 2 DAY, 0),
  (12, 16, 'Truco: en métodos numéricos, verifica siempre la convergencia del método de Newton-Raphson antes de implementarlo.', NOW() - INTERVAL 1 DAY, 0),
  (12, 14, 'Consejo: memoriza las complejidades de los algoritmos de ordenamiento; cada semestre caen en los exámenes.', NOW() - INTERVAL 12 HOUR, 0);

SET @tipArq = (SELECT id_tip FROM tips_academicos WHERE contenido LIKE 'Consejo: para los exámenes de arquitectura%');
SET @tipSql = (SELECT id_tip FROM tips_academicos WHERE contenido LIKE 'Tip: en SQL practica%');
SET @tipNew = (SELECT id_tip FROM tips_academicos WHERE contenido LIKE 'Truco: en métodos numéricos%');

-- El estudiante vota los tips del profesor (el trigger incrementa total_votos)
INSERT INTO votos_tips (id_tip, id_usuario, fecha_voto) VALUES
  (@tipArq, 12, NOW() - INTERVAL 2 DAY),
  (@tipSql, 12, NOW() - INTERVAL 1 DAY),
  (@tipNew, 12, NOW() - INTERVAL 12 HOUR);
