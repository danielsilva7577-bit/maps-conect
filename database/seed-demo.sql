-- MAPS Connect · Datos de demostración
-- Población de las tablas de contenido social (foro, recursos, círculos,
-- certificados y reseñas empresariales) para que el frontend muestre datos.
-- Usuario demo: Daniel Alejandro Silva Rosas
-- (id_usuario=12, id_estudiante=6, id_carrera=3, semestre=3).
-- Script idempotente: elimina primero los datos demo que él mismo crea.

USE maps_conect;

-- ---- Estudiante demo del Semestre Empresarial (autor de reseñas) ----
-- El trigger exige semestre >= 6 para reseñar; usamos un estudiante de semestre 7.
SET @resenaEstudiante = (SELECT e.id_estudiante FROM estudiantes e
    JOIN usuarios u ON u.id_usuario = e.id_usuario
    WHERE u.correo = 'semestre.empresarial.demo@tecmilenio.mx');

-- ---- Limpieza de datos demo (tablas alimentadas por este seed) ----
DELETE FROM resenas_empresarial WHERE id_estudiante = COALESCE(@resenaEstudiante, -1) OR id_estudiante = 6;
DELETE FROM estudiantes WHERE id_estudiante = @resenaEstudiante;
DELETE FROM usuarios WHERE correo = 'semestre.empresarial.demo@tecmilenio.mx';
DELETE FROM miembros_comunidad;
DELETE FROM comunidades_estudio;
DELETE FROM recursos_academicos;
DELETE FROM publicaciones;
DELETE FROM respuestas WHERE id_usuario = 12;
DELETE FROM estudiante_certificados WHERE id_estudiante = 6;
DELETE FROM certificados WHERE id_certificado IN (
  SELECT id_certificado FROM (
    SELECT c.id_certificado FROM certificados c
    WHERE c.nombre_certificado IN ('Certificado de Desarrollo Web Front-End','Certificado de Ciencia de Datos Empresarial')
  ) tmp
);
DELETE FROM estudiantes_materias WHERE id_estudiante = 6;

-- ---- 1) Materias del estudiante (dashboard "Mis materias") ----
INSERT INTO estudiantes_materias (id_estudiante, id_materia, fecha_seleccion) VALUES
  (6, 13, NOW() - INTERVAL 100 DAY),
  (6, 14, NOW() - INTERVAL 100 DAY),
  (6, 15, NOW() - INTERVAL 100 DAY),
  (6, 16, NOW() - INTERVAL 100 DAY),
  (6, 17, NOW() - INTERVAL 100 DAY);

-- ---- 2) Certificados (perfil) ----
INSERT INTO certificados (nombre_certificado, descripcion) VALUES
  ('Certificado de Desarrollo Web Front-End', 'Diseño e implementación de interfaces web con HTML, CSS, JavaScript y React.'),
  ('Certificado de Ciencia de Datos Empresarial', 'Análisis de datos, visualización y fundamentos de inteligencia de negocios.');

INSERT INTO estudiante_certificados (id_estudiante, id_certificado, fecha_seleccion) VALUES
  (6, (SELECT id_certificado FROM certificados WHERE nombre_certificado = 'Certificado de Desarrollo Web Front-End'), NOW() - INTERVAL 30 DAY),
  (6, (SELECT id_certificado FROM certificados WHERE nombre_certificado = 'Certificado de Ciencia de Datos Empresarial'), NOW() - INTERVAL 15 DAY);

-- ---- 3) Publicaciones del foro (foro + inicio) ----
INSERT INTO publicaciones (id_estudiante, id_carrera, id_materia, titulo, contenido, fecha_publicacion, estado) VALUES
  (6, 3, 13, 'Duda sobre normalización en bases de datos relacionales',
   '¿Alguien puede explicarme la diferencia práctica entre la primera y la segunda forma normal con un ejemplo sencillo?', NOW() - INTERVAL 2 DAY, 'resuelta'),
  (6, 3, 14, 'Complejidad de la búsqueda binaria vs. lineal',
   'Para un arreglo ordenado muy grande, ¿cuándo conviene realmente usar búsqueda binaria? ¿Merece la pena la complejidad adicional de implementación?', NOW() - INTERVAL 1 DAY, 'abierta'),
  (6, 3, 16, 'Métodos de integración numérica',
   'Estoy repasando para el examen de métodos numéricos. ¿Alguien tiene un apunte bueno sobre el método de Simpson o de trapecios?', NOW() - INTERVAL 5 HOUR, 'abierta'),
  (6, 3, 17, 'Patrones de diseño para arquitectura de software',
   'Comparto este resumen sobre patrones de diseño (Singleton, Factory, Observer). Espero que les sirva de repaso.', NOW() - INTERVAL 6 HOUR, 'abierta'),
  (6, 3, NULL, 'Consejos para el proyecto de Semana de Desarrollo Integral',
   'Estoy organizando un equipo para la semana de desarrollo integral. ¿Qué herramientas de colaboración recomiendan?', NOW() - INTERVAL 3 DAY, 'resuelta');

-- ---- Respuestas del foro ----
INSERT INTO respuestas (id_publicacion, id_usuario, contenido, fecha_respuesta, es_verificada_docente, es_solucion) VALUES
  ((SELECT id_publicacion FROM publicaciones WHERE titulo = 'Duda sobre normalización en bases de datos relacionales'), 12,
   'La 1FN elimina grupos repetidos y garantiza valores atómicos; la 2FN además elimina dependencias parciales. Ejemplo: una tabla de pedidos con datos del cliente viola la 2FN.', NOW() - INTERVAL 2 DAY, 0, 1),
  ((SELECT id_publicacion FROM publicaciones WHERE titulo = 'Complejidad de la búsqueda binaria vs. lineal'), 12,
   'Con arreglos ordenados la búsqueda binaria es O(log n) y casi siempre gana para n medianos/grandes; la ganancia se nota al repetir las búsquedas.', NOW() - INTERVAL 1 DAY, 0, 0),
  ((SELECT id_publicacion FROM publicaciones WHERE titulo = 'Consejos para el proyecto de Semana de Desarrollo Integral'), 12,
   'Usamos Notion para la planeación, Discord para la comunicación y GitHub para el código. ¡Éxito en tu proyecto!', NOW() - INTERVAL 2 DAY, 1, 1);

-- Marcar publicaciones resueltas apuntando a su respuesta aceptada
UPDATE publicaciones p
JOIN respuestas r ON r.contenido LIKE 'La 1FN elimina%'
SET p.id_respuesta_aceptada = r.id_respuesta
WHERE p.titulo = 'Duda sobre normalización en bases de datos relacionales';

UPDATE publicaciones p
JOIN respuestas r ON r.contenido LIKE 'Usamos Notion%'
SET p.id_respuesta_aceptada = r.id_respuesta
WHERE p.titulo = 'Consejos para el proyecto de Semana de Desarrollo Integral';

-- ---- 4) Recursos académicos (apuntes) ----
INSERT INTO recursos_academicos (id_usuario, id_materia, titulo, descripcion, url_archivo, tipo_archivo, fecha_subida, contador_descargas, contador_reportes, oculto) VALUES
  (12, 14, 'Algoritmos avanzados: guía de repaso', 'Resumen de complejidad, recursión, divide y vencerás y programación dinámica con ejemplos resueltos.', 'https://drive.example.com/algoritmos-avanzados.pdf', 'PDF', NOW() - INTERVAL 20 DAY, 34, 0, 0),
  (12, 13, 'Bases de datos SQL: ejercicios resueltos', 'Colección de consultas con JOIN, subconsultas y agregaciones comentadas paso a paso.', 'https://drive.example.com/sql-ejercicios.pdf', 'PDF', NOW() - INTERVAL 15 DAY, 51, 0, 0),
  (12, 17, 'Diagramas UML para arquitectura de software', 'Plantillas y ejemplos de diagramas de clases, componentes y despliegue.', 'https://drive.example.com/uml-plantillas.pptx', 'PPT', NOW() - INTERVAL 10 DAY, 22, 0, 0),
  (12, 16, 'Métodos numéricos: fórmulas y tablas', 'Hoja de referencia con los métodos de bisección, Newton-Raphson, trapecios y Simpson.', 'https://drive.example.com/metodos-numericos.xlsx', 'XLS', NOW() - INTERVAL 6 DAY, 18, 0, 0);

-- ---- 5) Círculos de estudio (comunidades + miembros) ----
INSERT INTO comunidades_estudio (nombre_comunidad, id_creador, id_carrera, id_materia, id_certificado, privacidad, enlace_sala_virtual) VALUES
  ('Círculo de Estructuras de Datos', 12, 3, 9, NULL, 'publica', NULL),
  ('Preparación Examen de Métodos Numéricos', 12, 3, 16, NULL, 'publica', NULL);

INSERT INTO miembros_comunidad (id_comunidad, id_usuario, rol_en_comunidad, fecha_union) VALUES
  ((SELECT id_comunidad FROM comunidades_estudio WHERE nombre_comunidad = 'Círculo de Estructuras de Datos'), 12, 'lider', NOW() - INTERVAL 20 DAY),
  ((SELECT id_comunidad FROM comunidades_estudio WHERE nombre_comunidad = 'Preparación Examen de Métodos Numéricos'), 12, 'lider', NOW() - INTERVAL 8 DAY);

-- ---- 6) Reseñas del Semestre Empresarial (calificaciones y experiencia) ----
-- Crea (idempotente) un estudiante de semestre 7 como autor de las reseñas para
-- respetar el trigger trg_resenas_empresarial_semestre_minimo.
INSERT INTO usuarios (nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, activo)
VALUES ('Estudiante Semestre Empresarial', 'semestre.empresarial.demo@tecmilenio.mx',
        '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy', 'ESTUDIANTE', 0, 1)
ON DUPLICATE KEY UPDATE id_usuario = LAST_INSERT_ID(id_usuario), nombre_completo = VALUES(nombre_completo);
SET @resenaUsuario = LAST_INSERT_ID();

INSERT INTO estudiantes (id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (@resenaUsuario, 3, 'AEPDEMO01', 7, 'Vivir una experiencia profesional de impacto en el sector tecnológico.')
ON DUPLICATE KEY UPDATE id_estudiante = LAST_INSERT_ID(id_estudiante);
SET @resenaEstudiante = LAST_INSERT_ID();

-- Eliminar reseñas previas del autor (idempotencia real: el id del estudiante
-- puede variar entre ejecuciones si el usuario demo fue borrado previamente).
DELETE FROM resenas_empresarial WHERE id_estudiante = @resenaEstudiante;
-- Reseñas huérfanas que apuntan a estudiantes ya eliminados (limpieza).
DELETE FROM resenas_empresarial WHERE id_estudiante NOT IN (SELECT id_estudiante FROM estudiantes);

INSERT INTO resenas_empresarial (id_estudiante, id_empresa, calificacion, proyecto_desarrollado, aprendizajes, recomendaciones, fecha_resena) VALUES
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IBM'), 5, 'Dashboard de monitoreo de servicios en la nube usando IBM Cloud.',
   'Aprendí a trabajar con microservicios y cómo escalar aplicaciones en entornos corporativos grandes. Excelente mentoría.',
   'Lleguen con bases sólidas de Docker y una actitud de aprendizaje activo.', NOW() - INTERVAL 60 DAY),
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'IBM'), 4, 'Automatización de reportes con scripts internos.', 'Reforcé mis habilidades de scripting y el trabajo en equipos distribuidos.', NULL, NOW() - INTERVAL 30 DAY),
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Tata Consultancy Services (TCS)'), 4, 'Soporte y desarrollo de funcionalidades en una plataforma bancaria.',
   'Aprendí el ciclo completo de entrega dentro de un equipo de desarrollo grande.', 'Practiquen testing y comunicación en inglés.', NOW() - INTERVAL 45 DAY),
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Softtek'), 4, 'Prototipo web para transformación digital de un cliente.', 'Experimenté los procesos de consultoría y la cercanía directa con el cliente final.', NULL, NOW() - INTERVAL 20 DAY),
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'Infosys'), 5, 'Análisis de datos para optimización de procesos internos.',
   'Gran aprendizaje en manejo de grandes volúmenes de datos y herramientas analíticas.', 'Dominar SQL y Excel avanzado es muy valorado.', NOW() - INTERVAL 15 DAY),
  (@resenaEstudiante, (SELECT id_empresa FROM empresas_vinculadas WHERE nombre_empresa = 'HubSpot'), 5, 'Colaboración en un proyecto de integración de CRM.',
   'Viví una cultura de innovación muy ágil; aprendí mucho sobre producto y colaboración remota.', NULL, NOW() - INTERVAL 10 DAY);
