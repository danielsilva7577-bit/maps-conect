-- =============================================================================
-- MAPS Connect — Esquema inicial de base de datos
-- Versión: V1 (baseline)
-- Motor:   MySQL 8.x / utf8mb4
--
-- INSTRUCCIONES DE USO:
--   Este script es la referencia del esquema. Se ejecuta manualmente o
--   mediante Flyway/Liquibase si se integra en el futuro.
--   Con `spring.jpa.hibernate.ddl-auto=validate` la app verifica que el
--   esquema de BD coincida con las entidades JPA en cada arranque.
--
-- Para crear la base de datos desde cero:
--   mysql -u root -p < V1__schema_inicial.sql
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ──────────────────────────────────────────────────────────────────────────────
-- 1. USUARIOS (cuenta base — email, contraseña, rol)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario        INT            NOT NULL AUTO_INCREMENT,
    correo            VARCHAR(150)   NOT NULL,
    nombre_completo   VARCHAR(150)   NOT NULL,
    contrasena_hash   VARCHAR(255)   NOT NULL,
    rol               VARCHAR(20)    NOT NULL COMMENT 'ESTUDIANTE | PROFESOR | ADMINISTRADOR',
    puntos_reputacion INT            NOT NULL DEFAULT 0,
    foto_url          VARCHAR(500)   NULL,
    activo            TINYINT(1)     NOT NULL DEFAULT 1,
    fecha_registro    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_correo (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 2. CARRERAS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS carreras (
    id_carrera   INT           NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(200)  NOT NULL,
    clave        VARCHAR(20)   NULL,
    modalidad    VARCHAR(50)   NULL,
    PRIMARY KEY (id_carrera)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 3. MATERIAS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS materias (
    id_materia   INT           NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(200)  NOT NULL,
    clave        VARCHAR(20)   NULL,
    semestre     INT           NULL,
    id_carrera   INT           NULL,
    PRIMARY KEY (id_materia),
    CONSTRAINT fk_materia_carrera FOREIGN KEY (id_carrera) REFERENCES carreras (id_carrera)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 4. PLAN DE ESTUDIOS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS plan_estudios (
    id_plan      INT  NOT NULL AUTO_INCREMENT,
    id_carrera   INT  NOT NULL,
    id_materia   INT  NOT NULL,
    semestre     INT  NOT NULL,
    PRIMARY KEY (id_plan),
    CONSTRAINT fk_plan_carrera FOREIGN KEY (id_carrera) REFERENCES carreras (id_carrera),
    CONSTRAINT fk_plan_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 5. CERTIFICADOS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS certificados (
    id_certificado   INT           NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(200)  NOT NULL,
    descripcion      TEXT          NULL,
    proveedor        VARCHAR(100)  NULL,
    PRIMARY KEY (id_certificado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 6. CERTIFICADO-MATERIA (relación: qué certificados están asociados a qué materias)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS certificado_materia (
    id              INT  NOT NULL AUTO_INCREMENT,
    id_certificado  INT  NOT NULL,
    id_materia      INT  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_certmat_cert FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado),
    CONSTRAINT fk_certmat_mat  FOREIGN KEY (id_materia)     REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 7. ESTUDIANTES (perfil académico del usuario con rol ESTUDIANTE)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS estudiantes (
    id_estudiante    INT          NOT NULL AUTO_INCREMENT,
    id_usuario       INT          NOT NULL,
    matricula        VARCHAR(20)  NOT NULL,
    id_carrera       INT          NULL,
    semestre_actual  INT          NULL,
    PRIMARY KEY (id_estudiante),
    UNIQUE KEY uq_matricula (matricula),
    UNIQUE KEY uq_est_usuario (id_usuario),
    CONSTRAINT fk_est_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_est_carrera  FOREIGN KEY (id_carrera)  REFERENCES carreras (id_carrera)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 8. ESTUDIANTE-MATERIA (materias inscritas por el estudiante)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS estudiante_materia (
    id             INT  NOT NULL AUTO_INCREMENT,
    id_estudiante  INT  NOT NULL,
    id_materia     INT  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_estmat_est FOREIGN KEY (id_estudiante) REFERENCES estudiantes (id_estudiante),
    CONSTRAINT fk_estmat_mat FOREIGN KEY (id_materia)    REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 9. ESTUDIANTE-CERTIFICADO (certificados obtenidos por el estudiante)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS estudiante_certificado (
    id              INT  NOT NULL AUTO_INCREMENT,
    id_estudiante   INT  NOT NULL,
    id_certificado  INT  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_estcert_est  FOREIGN KEY (id_estudiante)  REFERENCES estudiantes (id_estudiante),
    CONSTRAINT fk_estcert_cert FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 10. PROFESORES (perfil académico del usuario con rol PROFESOR)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS profesores (
    id_profesor          INT           NOT NULL AUTO_INCREMENT,
    id_usuario           INT           NOT NULL,
    numero_nomina        VARCHAR(30)   NOT NULL,
    area_especialidad    VARCHAR(200)  NULL,
    semestres_asignados  VARCHAR(100)  NULL,
    horario_asesorias    VARCHAR(200)  NULL,
    enlace_sala_virtual  VARCHAR(500)  NULL,
    biografia            TEXT          NULL,
    disponible_chat      TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_profesor),
    UNIQUE KEY uq_nomina   (numero_nomina),
    UNIQUE KEY uq_prof_usr (id_usuario),
    CONSTRAINT fk_prof_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 11. PROFESOR-MATERIA (materias que imparte el profesor)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS profesor_materia (
    id               INT          NOT NULL AUTO_INCREMENT,
    id_profesor      INT          NOT NULL,
    id_materia       INT          NOT NULL,
    ciclo_academico  VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_profmat_prof FOREIGN KEY (id_profesor) REFERENCES profesores (id_profesor),
    CONSTRAINT fk_profmat_mat  FOREIGN KEY (id_materia)  REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 12. PROFESOR-CERTIFICADO (certificados que tiene el profesor)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS profesor_certificado (
    id              INT  NOT NULL AUTO_INCREMENT,
    id_profesor     INT  NOT NULL,
    id_certificado  INT  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_profcert_prof FOREIGN KEY (id_profesor)    REFERENCES profesores (id_profesor),
    CONSTRAINT fk_profcert_cert FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 13. SEGUIMIENTOS (sistema de seguimiento/seguidos entre usuarios)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS seguimientos (
    id             INT       NOT NULL AUTO_INCREMENT,
    id_seguidor    INT       NOT NULL,
    id_seguido     INT       NOT NULL,
    fecha          DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_seguimiento (id_seguidor, id_seguido),
    CONSTRAINT fk_seg_seguidor FOREIGN KEY (id_seguidor) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_seg_seguido  FOREIGN KEY (id_seguido)  REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 14. CONVERSACIONES (chat 1 a 1)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS conversaciones (
    id             INT       NOT NULL AUTO_INCREMENT,
    id_usuario1    INT       NOT NULL,
    id_usuario2    INT       NOT NULL,
    fecha_inicio   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_conversacion (id_usuario1, id_usuario2),
    CONSTRAINT fk_conv_u1 FOREIGN KEY (id_usuario1) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_conv_u2 FOREIGN KEY (id_usuario2) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 15. MENSAJES (mensajes de chat privado)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS mensajes (
    id                INT           NOT NULL AUTO_INCREMENT,
    id_conversacion   INT           NOT NULL,
    id_emisor         INT           NOT NULL,
    contenido         TEXT          NULL,
    adjunto_nombre    VARCHAR(255)  NULL,
    adjunto_tipo      VARCHAR(100)  NULL,
    adjunto_tamano    BIGINT        NULL,
    leido             TINYINT(1)    NOT NULL DEFAULT 0,
    fecha_envio       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_msg_conv (id_conversacion),
    CONSTRAINT fk_msg_conv   FOREIGN KEY (id_conversacion) REFERENCES conversaciones (id),
    CONSTRAINT fk_msg_emisor FOREIGN KEY (id_emisor)       REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 16. NOTIFICACIONES (centro de notificaciones persistido)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notificaciones (
    id               INT           NOT NULL AUTO_INCREMENT,
    id_usuario       INT           NOT NULL,
    tipo             VARCHAR(50)   NOT NULL COMMENT 'mensaje | foro | sesion | asesoria | ...',
    titulo           VARCHAR(200)  NOT NULL,
    preview          VARCHAR(300)  NULL,
    enlace           VARCHAR(500)  NULL,
    leida            TINYINT(1)    NOT NULL DEFAULT 0,
    id_origen        INT           NULL COMMENT 'ID del elemento que originó la notificación',
    fecha_creacion   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notif_usuario (id_usuario),
    CONSTRAINT fk_notif_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 17. PUBLICACIONES (foro / feed)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS publicaciones (
    id               INT           NOT NULL AUTO_INCREMENT,
    id_autor         INT           NOT NULL,
    titulo           VARCHAR(300)  NULL,
    contenido        TEXT          NOT NULL,
    tipo             VARCHAR(50)   NULL,
    fecha_publicacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_pub_autor (id_autor),
    CONSTRAINT fk_pub_autor FOREIGN KEY (id_autor) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 18. RESPUESTAS (respuestas del foro)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS respuestas (
    id               INT       NOT NULL AUTO_INCREMENT,
    id_publicacion   INT       NOT NULL,
    id_autor         INT       NOT NULL,
    contenido        TEXT      NOT NULL,
    fecha_respuesta  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_resp_pub (id_publicacion),
    CONSTRAINT fk_resp_pub   FOREIGN KEY (id_publicacion) REFERENCES publicaciones (id),
    CONSTRAINT fk_resp_autor FOREIGN KEY (id_autor)       REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 19. TIPS ACADÉMICOS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tips_academicos (
    id          INT           NOT NULL AUTO_INCREMENT,
    id_autor    INT           NOT NULL,
    titulo      VARCHAR(300)  NOT NULL,
    contenido   TEXT          NOT NULL,
    id_materia  INT           NULL,
    votos       INT           NOT NULL DEFAULT 0,
    fecha       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_tip_autor   FOREIGN KEY (id_autor)   REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_tip_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 20. VOTOS DE TIPS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS votos_tip (
    id         INT  NOT NULL AUTO_INCREMENT,
    id_tip     INT  NOT NULL,
    id_usuario INT  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_voto (id_tip, id_usuario),
    CONSTRAINT fk_voto_tip     FOREIGN KEY (id_tip)     REFERENCES tips_academicos (id),
    CONSTRAINT fk_voto_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 21. RECURSOS ACADÉMICOS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS recursos_academicos (
    id            INT           NOT NULL AUTO_INCREMENT,
    id_autor      INT           NOT NULL,
    titulo        VARCHAR(300)  NOT NULL,
    descripcion   TEXT          NULL,
    tipo          VARCHAR(50)   NULL COMMENT 'pdf | video | link | ...',
    url           VARCHAR(500)  NULL,
    id_materia    INT           NULL,
    fecha_subida  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_rec_autor   FOREIGN KEY (id_autor)   REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_rec_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 22. SESIONES DE REPASO (círculos de estudio)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sesiones_repaso (
    id              INT           NOT NULL AUTO_INCREMENT,
    id_organizador  INT           NOT NULL,
    id_materia      INT           NULL,
    id_recurso      INT           NULL,
    titulo          VARCHAR(300)  NOT NULL,
    descripcion     TEXT          NULL,
    fecha_sesion    DATETIME      NULL,
    enlace          VARCHAR(500)  NULL,
    cupo_maximo     INT           NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ses_org     FOREIGN KEY (id_organizador) REFERENCES usuarios (id_usuario),
    CONSTRAINT fk_ses_materia FOREIGN KEY (id_materia)     REFERENCES materias (id_materia),
    CONSTRAINT fk_ses_recurso FOREIGN KEY (id_recurso)     REFERENCES recursos_academicos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 23. SESIÓN-INSCRIPCIÓN (participantes de sesiones de repaso)
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sesion_inscripcion (
    id          INT       NOT NULL AUTO_INCREMENT,
    id_sesion   INT       NOT NULL,
    id_usuario  INT       NOT NULL,
    fecha       DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_inscripcion (id_sesion, id_usuario),
    CONSTRAINT fk_insc_sesion   FOREIGN KEY (id_sesion)  REFERENCES sesiones_repaso (id),
    CONSTRAINT fk_insc_usuario  FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 24. COMUNIDADES DE ESTUDIO
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS comunidades_estudio (
    id           INT           NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(200)  NOT NULL,
    descripcion  TEXT          NULL,
    id_creador   INT           NOT NULL,
    fecha_creacion DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_com_creador FOREIGN KEY (id_creador) REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 25. MIEMBRO-COMUNIDAD
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS miembros_comunidad (
    id            INT       NOT NULL AUTO_INCREMENT,
    id_comunidad  INT       NOT NULL,
    id_usuario    INT       NOT NULL,
    fecha_union   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_miembro (id_comunidad, id_usuario),
    CONSTRAINT fk_miem_com     FOREIGN KEY (id_comunidad) REFERENCES comunidades_estudio (id),
    CONSTRAINT fk_miem_usuario FOREIGN KEY (id_usuario)   REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 26. EMPRESAS VINCULADAS
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS empresas_vinculadas (
    id          INT           NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(200)  NOT NULL,
    descripcion TEXT          NULL,
    logo_url    VARCHAR(500)  NULL,
    sitio_web   VARCHAR(300)  NULL,
    sector      VARCHAR(100)  NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ──────────────────────────────────────────────────────────────────────────────
-- 27. RESEÑAS EMPRESARIALES
-- ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS resenas_empresariales (
    id           INT       NOT NULL AUTO_INCREMENT,
    id_empresa   INT       NOT NULL,
    id_autor     INT       NOT NULL,
    calificacion INT       NOT NULL COMMENT '1-5 estrellas',
    comentario   TEXT      NULL,
    fecha        DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_res_empresa FOREIGN KEY (id_empresa) REFERENCES empresas_vinculadas (id),
    CONSTRAINT fk_res_autor   FOREIGN KEY (id_autor)   REFERENCES usuarios (id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- FIN DEL ESQUEMA INICIAL
-- =============================================================================
