-- =====================================================================
-- MAPS Connect - Esquema de Base de Datos (MySQL 8.0, 3FN)
-- Universidad Tecmilenio - Plataforma Académica y de Mentoría MAPS
--
-- Basado en: "Documento de Especificación de Proyecto: Plataforma
-- Académica y de Mentoría MAPS" - Sección 6 (Catálogo de 22 Entidades)
-- y Sección 7 (Reglas de Integridad y Restricciones Técnicas).
--
-- Ejecutar dentro del esquema correspondiente, por ejemplo:
--   USE maps_conect_dev;   -- perfil dev
--   USE maps_conect;       -- perfil prod
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- Módulo 1: Gestión Académica y Modelo MAPS
-- =====================================================================

-- 1. carreras: programas profesionales ofertados
CREATE TABLE carreras (
    id_carrera      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    clave           VARCHAR(20)  NOT NULL,
    descripcion     TEXT,
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_carreras_nombre UNIQUE (nombre),
    CONSTRAINT uq_carreras_clave UNIQUE (clave)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. certificados: especializaciones y microcredenciales MAPS
CREATE TABLE certificados (
    id_certificado  INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    descripcion     TEXT,
    area            VARCHAR(100),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_certificados_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. materias: asignaturas curriculares del plan de estudios
CREATE TABLE materias (
    id_materia      INT AUTO_INCREMENT PRIMARY KEY,
    clave           VARCHAR(20)  NOT NULL,
    nombre          VARCHAR(150) NOT NULL,
    creditos        INT NOT NULL DEFAULT 0,
    tipo            ENUM('TRONCO_COMUN', 'DISCIPLINAR', 'CERTIFICADO', 'BIENESTAR') NOT NULL,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_materias_clave UNIQUE (clave),
    CONSTRAINT chk_materias_creditos CHECK (creditos >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 2: Usuarios, Perfiles y Roles
-- =====================================================================

-- 6. usuarios: credenciales, rol y autenticación general
CREATE TABLE usuarios (
    id_usuario           INT AUTO_INCREMENT PRIMARY KEY,
    correo               VARCHAR(150) NOT NULL,
    contrasena_hash      VARCHAR(255) NOT NULL,
    nombre               VARCHAR(100) NOT NULL,
    apellido             VARCHAR(100) NOT NULL,
    rol                  ENUM('ESTUDIANTE', 'DOCENTE', 'EGRESADO', 'ADMIN') NOT NULL,
    activo               BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion  DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    -- Restricción de Dominio Institucional
    CONSTRAINT chk_usuarios_correo_institucional CHECK (
        correo LIKE '%@tecmilenio.mx' OR correo LIKE '%@servicios.tecmilenio.mx'
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. plan_estudios: relación N:M de materias por carrera y semestre sugerido
CREATE TABLE plan_estudios (
    id_plan             INT AUTO_INCREMENT PRIMARY KEY,
    id_carrera          INT NOT NULL,
    id_materia          INT NOT NULL,
    semestre_sugerido   INT NOT NULL,
    CONSTRAINT fk_plan_estudios_carrera FOREIGN KEY (id_carrera) REFERENCES carreras (id_carrera) ON DELETE CASCADE,
    CONSTRAINT fk_plan_estudios_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE CASCADE,
    CONSTRAINT uq_plan_estudios UNIQUE (id_carrera, id_materia),
    -- Restricción de Rango Semestral
    CONSTRAINT chk_plan_estudios_semestre CHECK (semestre_sugerido BETWEEN 1 AND 12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. certificado_materias: relación N:M de asignaturas que integran cada certificado
CREATE TABLE certificado_materias (
    id_certificado_materia  INT AUTO_INCREMENT PRIMARY KEY,
    id_certificado          INT NOT NULL,
    id_materia              INT NOT NULL,
    CONSTRAINT fk_certificado_materias_certificado FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado) ON DELETE CASCADE,
    CONSTRAINT fk_certificado_materias_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE CASCADE,
    CONSTRAINT uq_certificado_materias UNIQUE (id_certificado, id_materia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. estudiantes: datos específicos del alumno (matrícula, semestre, propósito)
CREATE TABLE estudiantes (
    id_estudiante       INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario          INT NOT NULL,
    id_carrera          INT NULL,
    matricula           VARCHAR(20) NOT NULL,
    semestre_actual     INT NOT NULL,
    proposito_vida      TEXT,
    puntos_reputacion   INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_estudiantes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_estudiantes_carrera FOREIGN KEY (id_carrera) REFERENCES carreras (id_carrera) ON DELETE SET NULL,
    CONSTRAINT uq_estudiantes_usuario UNIQUE (id_usuario),
    CONSTRAINT uq_estudiantes_matricula UNIQUE (matricula),
    -- Restricción de Rango Semestral
    CONSTRAINT chk_estudiantes_semestre CHECK (semestre_actual BETWEEN 1 AND 12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. estudiante_certificados: ruta MAPS (hasta 3 certificados elegidos por el alumno)
CREATE TABLE estudiante_certificados (
    id_estudiante_certificado  INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante              INT NOT NULL,
    id_certificado             INT NOT NULL,
    fecha_eleccion             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_estudiante_certificados_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes (id_estudiante) ON DELETE CASCADE,
    CONSTRAINT fk_estudiante_certificados_certificado FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado) ON DELETE CASCADE,
    CONSTRAINT uq_estudiante_certificados UNIQUE (id_estudiante, id_certificado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. profesores: nómina, especialidad, materias asignadas, biografía y disponibilidad
CREATE TABLE profesores (
    id_profesor         INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario          INT NOT NULL,
    numero_nomina       VARCHAR(20) NOT NULL,
    area_especialidad   VARCHAR(150),
    biografia           TEXT,
    horario_asesorias   VARCHAR(255),
    disponible_chat     BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_profesores_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT uq_profesores_usuario UNIQUE (id_usuario),
    CONSTRAINT uq_profesores_nomina UNIQUE (numero_nomina)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. profesores_materias: relación N:M de materias impartidas por ciclo académico
CREATE TABLE profesores_materias (
    id_profesor_materia  INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor          INT NOT NULL,
    id_materia           INT NOT NULL,
    ciclo                VARCHAR(20) NOT NULL,
    CONSTRAINT fk_profesores_materias_profesor FOREIGN KEY (id_profesor) REFERENCES profesores (id_profesor) ON DELETE CASCADE,
    CONSTRAINT fk_profesores_materias_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE CASCADE,
    CONSTRAINT uq_profesores_materias UNIQUE (id_profesor, id_materia, ciclo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 3: Foro de Dudas y Respuestas Colaborativas
-- =====================================================================

-- 11. publicaciones: dudas académicas publicadas por estudiantes
CREATE TABLE publicaciones (
    id_publicacion          INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario              INT NOT NULL,
    id_carrera              INT NOT NULL,
    id_materia              INT NULL,
    titulo                  VARCHAR(200) NOT NULL,
    contenido               TEXT NOT NULL,
    id_respuesta_aceptada   INT NULL,
    estado                  ENUM('ABIERTA', 'RESUELTA', 'MODERADA') NOT NULL DEFAULT 'ABIERTA',
    fecha_creacion          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_publicaciones_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_publicaciones_carrera FOREIGN KEY (id_carrera) REFERENCES carreras (id_carrera) ON DELETE CASCADE,
    CONSTRAINT fk_publicaciones_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. respuestas: soluciones y comentarios de la comunidad a cada duda
CREATE TABLE respuestas (
    id_respuesta          INT AUTO_INCREMENT PRIMARY KEY,
    id_publicacion        INT NOT NULL,
    id_usuario            INT NOT NULL,
    contenido             TEXT NOT NULL,
    es_solucion           BOOLEAN NOT NULL DEFAULT FALSE,
    verificado_docente    BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_respuestas_publicacion FOREIGN KEY (id_publicacion) REFERENCES publicaciones (id_publicacion) ON DELETE CASCADE,
    CONSTRAINT fk_respuestas_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Enlace tardío: la publicación puede marcar una respuesta como solución aceptada
ALTER TABLE publicaciones
    ADD CONSTRAINT fk_publicaciones_respuesta_aceptada
        FOREIGN KEY (id_respuesta_aceptada) REFERENCES respuestas (id_respuesta) ON DELETE SET NULL;

-- =====================================================================
-- Módulo 4: Tips Académicos y Sistema de Reputación
-- =====================================================================

-- 13. tips_academicos: recomendaciones y técnicas de estudio por materia
CREATE TABLE tips_academicos (
    id_tip          INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT NOT NULL,
    id_materia      INT NULL,
    contenido       TEXT NOT NULL,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tips_academicos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_tips_academicos_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. votos_tips: control de votos únicos por usuario para ranking de tips
CREATE TABLE votos_tips (
    id_voto      INT AUTO_INCREMENT PRIMARY KEY,
    id_tip       INT NOT NULL,
    id_usuario   INT NOT NULL,
    fecha_voto   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_votos_tips_tip FOREIGN KEY (id_tip) REFERENCES tips_academicos (id_tip) ON DELETE CASCADE,
    CONSTRAINT fk_votos_tips_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    -- Voto de utilidad único por usuario
    CONSTRAINT uq_votos_tips UNIQUE (id_tip, id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 5: Repositorio de Apuntes y Material de Estudio
-- =====================================================================

-- 15. recursos_academicos: metadatos de archivos y apuntes subidos por materia
CREATE TABLE recursos_academicos (
    id_recurso       INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT NOT NULL,
    id_materia       INT NOT NULL,
    titulo           VARCHAR(200) NOT NULL,
    descripcion      TEXT,
    url_archivo      VARCHAR(255) NOT NULL,
    tipo_archivo     VARCHAR(50),
    descargas        INT NOT NULL DEFAULT 0,
    reportes         INT NOT NULL DEFAULT 0,
    oculto           BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_subida     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recursos_academicos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_recursos_academicos_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE CASCADE,
    CONSTRAINT chk_recursos_academicos_descargas CHECK (descargas >= 0),
    CONSTRAINT chk_recursos_academicos_reportes CHECK (reportes >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 6: Mensajería Directa y Asesorías Privadas
-- =====================================================================

-- 16. conversaciones: registro del canal 1 a 1 entre dos usuarios distintos
CREATE TABLE conversaciones (
    id_conversacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_1     INT NOT NULL,
    id_usuario_2     INT NOT NULL,
    fecha_inicio     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversaciones_usuario_1 FOREIGN KEY (id_usuario_1) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_conversaciones_usuario_2 FOREIGN KEY (id_usuario_2) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    -- Exclusividad de Canal de Mensajería
    CONSTRAINT uq_conversaciones_usuarios UNIQUE (id_usuario_1, id_usuario_2),
    CONSTRAINT chk_conversaciones_usuarios_distintos CHECK (id_usuario_1 <> id_usuario_2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. mensajes: mensajes individuales enviados en una conversación
CREATE TABLE mensajes (
    id_mensaje         INT AUTO_INCREMENT PRIMARY KEY,
    id_conversacion    INT NOT NULL,
    id_usuario_emisor  INT NOT NULL,
    contenido          TEXT NOT NULL,
    leido              BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mensajes_conversacion FOREIGN KEY (id_conversacion) REFERENCES conversaciones (id_conversacion) ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_usuario_emisor FOREIGN KEY (id_usuario_emisor) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 7: Semestre Empresarial
-- =====================================================================

-- 18. empresas_vinculadas: directorio de empresas receptoras de Semestre Empresarial
CREATE TABLE empresas_vinculadas (
    id_empresa      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    sector          VARCHAR(100),
    descripcion     TEXT,
    activa          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_empresas_vinculadas_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. resenas_empresarial: evaluaciones, aprendizajes y consejos de estudiantes
CREATE TABLE resenas_empresarial (
    id_resena            INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante        INT NOT NULL,
    id_empresa           INT NOT NULL,
    calificacion         TINYINT NOT NULL,
    proyecto_realizado   TEXT,
    recomendaciones      TEXT,
    fecha_resena         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resenas_empresarial_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes (id_estudiante) ON DELETE CASCADE,
    CONSTRAINT fk_resenas_empresarial_empresa FOREIGN KEY (id_empresa) REFERENCES empresas_vinculadas (id_empresa) ON DELETE CASCADE,
    -- Calificaciones Numéricas
    CONSTRAINT chk_resenas_empresarial_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- Módulo 8: Comunidades y Círculos de Estudio
-- =====================================================================

-- 20. comunidades_estudio: grupos de estudio por materia, certificado o carrera
CREATE TABLE comunidades_estudio (
    id_comunidad          INT AUTO_INCREMENT PRIMARY KEY,
    nombre                VARCHAR(150) NOT NULL,
    id_materia            INT NULL,
    id_certificado         INT NULL,
    id_usuario_creador    INT NOT NULL,
    privacidad            ENUM('PUBLICA', 'PRIVADA') NOT NULL DEFAULT 'PUBLICA',
    enlace_sala_virtual   VARCHAR(255),
    fecha_creacion        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comunidades_estudio_materia FOREIGN KEY (id_materia) REFERENCES materias (id_materia) ON DELETE SET NULL,
    CONSTRAINT fk_comunidades_estudio_certificado FOREIGN KEY (id_certificado) REFERENCES certificados (id_certificado) ON DELETE SET NULL,
    CONSTRAINT fk_comunidades_estudio_creador FOREIGN KEY (id_usuario_creador) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 21. miembros_comunidad: relación N:M de usuarios inscritos en cada círculo de estudio
CREATE TABLE miembros_comunidad (
    id_miembro       INT AUTO_INCREMENT PRIMARY KEY,
    id_comunidad     INT NOT NULL,
    id_usuario       INT NOT NULL,
    rol_comunidad    ENUM('LIDER', 'ASESOR', 'MIEMBRO') NOT NULL DEFAULT 'MIEMBRO',
    fecha_union      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Integridad en Círculos de Estudio (ON DELETE CASCADE)
    CONSTRAINT fk_miembros_comunidad_comunidad FOREIGN KEY (id_comunidad) REFERENCES comunidades_estudio (id_comunidad) ON DELETE CASCADE,
    CONSTRAINT fk_miembros_comunidad_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE,
    CONSTRAINT uq_miembros_comunidad UNIQUE (id_comunidad, id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 22. mensajes_comunidad: muro de publicaciones y avisos internos de cada comunidad
CREATE TABLE mensajes_comunidad (
    id_mensaje_comunidad  INT AUTO_INCREMENT PRIMARY KEY,
    id_comunidad          INT NOT NULL,
    id_usuario            INT NOT NULL,
    contenido             TEXT NOT NULL,
    fecha_publicacion     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Integridad en Círculos de Estudio (ON DELETE CASCADE)
    CONSTRAINT fk_mensajes_comunidad_comunidad FOREIGN KEY (id_comunidad) REFERENCES comunidades_estudio (id_comunidad) ON DELETE CASCADE,
    CONSTRAINT fk_mensajes_comunidad_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- Índices adicionales para columnas de búsqueda frecuente
-- =====================================================================

CREATE INDEX idx_publicaciones_carrera ON publicaciones (id_carrera);
CREATE INDEX idx_publicaciones_materia ON publicaciones (id_materia);
CREATE INDEX idx_respuestas_publicacion ON respuestas (id_publicacion);
CREATE INDEX idx_tips_academicos_materia ON tips_academicos (id_materia);
CREATE INDEX idx_recursos_academicos_materia ON recursos_academicos (id_materia);
CREATE INDEX idx_mensajes_conversacion ON mensajes (id_conversacion);
CREATE INDEX idx_comunidades_estudio_materia ON comunidades_estudio (id_materia);
CREATE INDEX idx_comunidades_estudio_certificado ON comunidades_estudio (id_certificado);

-- =====================================================================
-- Triggers de reglas de negocio que no pueden expresarse con CHECK
-- =====================================================================

DELIMITER $$

-- Límite de Ruta Curricular: máximo 3 certificados simultáneos por estudiante
CREATE TRIGGER trg_estudiante_certificados_limite
BEFORE INSERT ON estudiante_certificados
FOR EACH ROW
BEGIN
    DECLARE total_certificados INT;
    SELECT COUNT(*) INTO total_certificados
    FROM estudiante_certificados
    WHERE id_estudiante = NEW.id_estudiante;

    IF total_certificados >= 3 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Un estudiante solo puede vincular un máximo de 3 certificados simultáneos.';
    END IF;
END$$

-- Insignia de verificación automática para respuestas aportadas por docentes
CREATE TRIGGER trg_respuestas_verificacion_docente
BEFORE INSERT ON respuestas
FOR EACH ROW
BEGIN
    DECLARE autor_rol VARCHAR(20);
    SELECT rol INTO autor_rol FROM usuarios WHERE id_usuario = NEW.id_usuario;

    IF autor_rol = 'DOCENTE' THEN
        SET NEW.verificado_docente = TRUE;
    END IF;
END$$

-- Reseñar Semestre Empresarial: solo estudiantes con semestre >= 6
CREATE TRIGGER trg_resenas_empresarial_semestre_minimo
BEFORE INSERT ON resenas_empresarial
FOR EACH ROW
BEGIN
    DECLARE semestre_estudiante INT;
    SELECT semestre_actual INTO semestre_estudiante
    FROM estudiantes
    WHERE id_estudiante = NEW.id_estudiante;

    IF semestre_estudiante IS NULL OR semestre_estudiante < 6 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo estudiantes de semestre 6 o superior pueden reseñar el Semestre Empresarial.';
    END IF;
END$$

DELIMITER ;
