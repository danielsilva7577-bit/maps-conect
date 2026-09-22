-- ============================================================
-- MAPS Connect - Limpieza de datos ficticios (demo)
-- Elimina SOLO los datos demo sembrados por seed-demo.sql y
-- seed-mensajes-tips.sql. Conserva los datos reales originales
-- (empresas, materias, carreras, plan_estudios y el usuario real).
-- Idempotente: puede ejecutarse varias veces sin dañar nada.
-- ============================================================

USE maps_conect;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;

-- Reservas / comunidades (semilla demo)
DELETE FROM miembros_comunidad;
DELETE FROM comunidades_estudio;

-- Reseñas empresariales (semilla demo)
DELETE FROM resenas_empresarial;

-- Recursos académicos (semilla demo)
DELETE FROM recursos_academicos;

-- Foro: respuestas y publicaciones (semilla demo)
DELETE FROM respuestas;
DELETE FROM publicaciones;

-- Certificados del estudiante y catálogo de certificados demo
DELETE FROM estudiante_certificados;
DELETE FROM certificados;

-- Materias asignadas al estudiante demo
DELETE FROM estudiantes_materias;

-- Votos de tips (por triggers ajustan total_votos de tips_academicos)
DELETE FROM votos_tips;

-- Tips académicos (semilla demo)
DELETE FROM tips_academicos;

-- Mensajes y conversaciones demo (exige borrar mensajes primero)
DELETE FROM mensajes;
DELETE FROM conversaciones;

-- Profesores creados por el seed demo (enlazados a usuarios ficticios)
DELETE FROM profesores WHERE id_usuario IN (
    SELECT id_usuario FROM usuarios WHERE correo IN (
        'profesor.demo@tecmilenio.mx',
        'semestre.empresarial.demo@tecmilenio.mx'
    )
);

-- Usuarios ficticios del seed (NUNCA el usuario real id=12 / al07080560@tecmilenio.mx)
DELETE FROM usuarios WHERE correo IN (
    'profesor.demo@tecmilenio.mx',
    'semestre.empresarial.demo@tecmilenio.mx'
);

SET FOREIGN_KEY_CHECKS = 1;
