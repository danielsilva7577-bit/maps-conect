
CREATE DATABASE IF NOT EXISTS maps_conect
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
USE maps_conect;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';

-- =====================================================================
-- 1) ESQUEMA
-- =====================================================================
DROP TABLE IF EXISTS `carreras`;
CREATE TABLE `carreras` (
  `id_carrera` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_carrera` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `clave_carrera` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_carrera`),
  UNIQUE KEY `uq_carreras_nombre` (`nombre_carrera`),
  UNIQUE KEY `uq_carreras_clave` (`clave_carrera`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `certificados`;
CREATE TABLE `certificados` (
  `id_certificado` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_certificado` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_certificado`),
  UNIQUE KEY `uq_certificados_nombre` (`nombre_certificado`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `certificado_materias`;
CREATE TABLE `certificado_materias` (
  `id_certificado_materia` int unsigned NOT NULL AUTO_INCREMENT,
  `id_certificado` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  PRIMARY KEY (`id_certificado_materia`),
  UNIQUE KEY `uq_certificado_materias` (`id_certificado`,`id_materia`),
  KEY `fk_certificado_materias_materia` (`id_materia`),
  CONSTRAINT `fk_certificado_materias_certificado` FOREIGN KEY (`id_certificado`) REFERENCES `certificados` (`id_certificado`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_certificado_materias_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `comunidades_estudio`;
CREATE TABLE `comunidades_estudio` (
  `id_comunidad` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_comunidad` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `id_creador` int unsigned NOT NULL,
  `id_carrera` int unsigned DEFAULT NULL,
  `id_materia` int unsigned DEFAULT NULL,
  `id_certificado` int unsigned DEFAULT NULL,
  `privacidad` enum('publica','privada') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'publica',
  `enlace_sala_virtual` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_comunidad`),
  KEY `fk_comunidades_estudio_creador` (`id_creador`),
  KEY `fk_comunidades_estudio_carrera` (`id_carrera`),
  KEY `fk_comunidades_estudio_materia` (`id_materia`),
  KEY `fk_comunidades_estudio_certificado` (`id_certificado`),
  CONSTRAINT `fk_comunidades_estudio_carrera` FOREIGN KEY (`id_carrera`) REFERENCES `carreras` (`id_carrera`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_comunidades_estudio_certificado` FOREIGN KEY (`id_certificado`) REFERENCES `certificados` (`id_certificado`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_comunidades_estudio_creador` FOREIGN KEY (`id_creador`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comunidades_estudio_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `conversaciones`;
CREATE TABLE `conversaciones` (
  `id_conversacion` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario_1` int unsigned NOT NULL,
  `id_usuario_2` int unsigned NOT NULL,
  `fecha_inicio` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_conversacion`),
  UNIQUE KEY `uq_conversaciones_par` (`id_usuario_1`,`id_usuario_2`),
  KEY `fk_conversaciones_usuario_2` (`id_usuario_2`),
  CONSTRAINT `fk_conversaciones_usuario_1` FOREIGN KEY (`id_usuario_1`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_conversaciones_usuario_2` FOREIGN KEY (`id_usuario_2`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_conversaciones_valida_par` BEFORE INSERT ON `conversaciones` FOR EACH ROW BEGIN
    IF NEW.id_usuario_1 >= NEW.id_usuario_2 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'id_usuario_1 debe ser menor que id_usuario_2 para evitar duplicados y auto-chats.';
    END IF;
END */;;
DELIMITER ;
DROP TABLE IF EXISTS `empresas_vinculadas`;
CREATE TABLE `empresas_vinculadas` (
  `id_empresa` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_empresa` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sector` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sitio_web` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `carreras_afines` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_empresa`),
  UNIQUE KEY `uq_empresas_vinculadas_nombre` (`nombre_empresa`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `estudiantes`;
CREATE TABLE `estudiantes` (
  `id_estudiante` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `id_carrera` int unsigned NOT NULL,
  `matricula` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `semestre_actual` tinyint unsigned NOT NULL,
  `proposito_vida` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id_estudiante`),
  UNIQUE KEY `uq_estudiantes_usuario` (`id_usuario`),
  UNIQUE KEY `uq_estudiantes_matricula` (`matricula`),
  KEY `fk_estudiantes_carrera` (`id_carrera`),
  CONSTRAINT `fk_estudiantes_carrera` FOREIGN KEY (`id_carrera`) REFERENCES `carreras` (`id_carrera`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_estudiantes_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_estudiantes_semestre` CHECK ((`semestre_actual` between 1 and 12))
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `estudiante_certificados`;
CREATE TABLE `estudiante_certificados` (
  `id_estudiante_certificado` int unsigned NOT NULL AUTO_INCREMENT,
  `id_estudiante` int unsigned NOT NULL,
  `id_certificado` int unsigned NOT NULL,
  `fecha_seleccion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_estudiante_certificado`),
  UNIQUE KEY `uq_estudiante_certificados` (`id_estudiante`,`id_certificado`),
  KEY `fk_estudiante_certificados_certificado` (`id_certificado`),
  CONSTRAINT `fk_estudiante_certificados_certificado` FOREIGN KEY (`id_certificado`) REFERENCES `certificados` (`id_certificado`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_estudiante_certificados_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_estudiante_certificados_max3` BEFORE INSERT ON `estudiante_certificados` FOR EACH ROW BEGIN
    DECLARE v_total INT;
    SELECT COUNT(*) INTO v_total
    FROM estudiante_certificados
    WHERE id_estudiante = NEW.id_estudiante;

    IF v_total >= 3 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Un estudiante solo puede tener hasta 3 certificados en su ruta MAPS.';
    END IF;
END */;;
DELIMITER ;
DROP TABLE IF EXISTS `estudiantes_materias`;
CREATE TABLE `estudiantes_materias` (
  `id_estudiante_materia` int unsigned NOT NULL AUTO_INCREMENT,
  `id_estudiante` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  `fecha_seleccion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_estudiante_materia`),
  UNIQUE KEY `uq_estudiantes_materias` (`id_estudiante`,`id_materia`),
  KEY `fk_estudiantes_materias_materia` (`id_materia`),
  CONSTRAINT `fk_estudiantes_materias_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_estudiantes_materias_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `materias`;
CREATE TABLE `materias` (
  `id_materia` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_materia` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `clave_materia` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `tipo_materia` enum('tronco_comun','disciplinar','certificado','bienestar') COLLATE utf8mb4_unicode_ci NOT NULL,
  `creditos` tinyint unsigned DEFAULT NULL,
  PRIMARY KEY (`id_materia`),
  UNIQUE KEY `uq_materias_clave` (`clave_materia`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `mensajes`;
CREATE TABLE `mensajes` (
  `id_mensaje` int unsigned NOT NULL AUTO_INCREMENT,
  `id_conversacion` int unsigned NOT NULL,
  `id_emisor` int unsigned NOT NULL,
  `contenido` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `adjunto_nombre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adjunto_tipo` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adjunto_tamano` bigint DEFAULT NULL,
  `fecha_envio` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `leido` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_mensaje`),
  KEY `fk_mensajes_emisor` (`id_emisor`),
  KEY `idx_mensajes_conversacion` (`id_conversacion`),
  CONSTRAINT `fk_mensajes_conversacion` FOREIGN KEY (`id_conversacion`) REFERENCES `conversaciones` (`id_conversacion`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_mensajes_emisor` FOREIGN KEY (`id_emisor`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `miembros_comunidad`;
CREATE TABLE `miembros_comunidad` (
  `id_miembro` int unsigned NOT NULL AUTO_INCREMENT,
  `id_comunidad` int unsigned NOT NULL,
  `id_usuario` int unsigned NOT NULL,
  `rol_en_comunidad` enum('lider','asesor','miembro') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'miembro',
  `fecha_union` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_miembro`),
  UNIQUE KEY `uq_miembros_comunidad` (`id_comunidad`,`id_usuario`),
  KEY `fk_miembros_comunidad_usuario` (`id_usuario`),
  KEY `idx_miembros_comunidad_id` (`id_comunidad`),
  CONSTRAINT `fk_miembros_comunidad_comunidad` FOREIGN KEY (`id_comunidad`) REFERENCES `comunidades_estudio` (`id_comunidad`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_miembros_comunidad_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `notificaciones`;
CREATE TABLE `notificaciones` (
  `id_notificacion` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `tipo` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'general',
  `titulo` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `preview` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enlace` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `leida` tinyint(1) NOT NULL DEFAULT '0',
  `id_origen` int unsigned DEFAULT NULL,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_notificacion`),
  KEY `fk_notificaciones_usuario` (`id_usuario`),
  KEY `idx_notificaciones_usuario_leida` (`id_usuario`,`leida`),
  CONSTRAINT `fk_notificaciones_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `plan_estudios`;
CREATE TABLE `plan_estudios` (
  `id_plan_estudios` int unsigned NOT NULL AUTO_INCREMENT,
  `id_carrera` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  `semestre_sugerido` tinyint unsigned NOT NULL,
  PRIMARY KEY (`id_plan_estudios`),
  UNIQUE KEY `uq_plan_estudios` (`id_carrera`,`id_materia`),
  KEY `fk_plan_estudios_materia` (`id_materia`),
  CONSTRAINT `fk_plan_estudios_carrera` FOREIGN KEY (`id_carrera`) REFERENCES `carreras` (`id_carrera`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_plan_estudios_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_plan_estudios_semestre` CHECK ((`semestre_sugerido` between 1 and 12))
) ENGINE=InnoDB AUTO_INCREMENT=311 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `profesores`;
CREATE TABLE `profesores` (
  `id_profesor` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `numero_nomina` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `area_especialidad` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `biografia` text COLLATE utf8mb4_unicode_ci,
  `horario_asesorias` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enlace_sala_virtual` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `semestres_asignados` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `disponible_chat` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_profesor`),
  UNIQUE KEY `uq_profesores_usuario` (`id_usuario`),
  UNIQUE KEY `uq_profesores_nomina` (`numero_nomina`),
  CONSTRAINT `fk_profesores_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `profesores_certificados`;
CREATE TABLE `profesores_certificados` (
  `id_profesor_certificado` int unsigned NOT NULL AUTO_INCREMENT,
  `id_profesor` int unsigned NOT NULL,
  `id_certificado` int unsigned NOT NULL,
  PRIMARY KEY (`id_profesor_certificado`),
  UNIQUE KEY `uq_profesores_certificados` (`id_profesor`,`id_certificado`),
  KEY `fk_profesores_certificados_certificado` (`id_certificado`),
  CONSTRAINT `fk_profesores_certificados_certificado` FOREIGN KEY (`id_certificado`) REFERENCES `certificados` (`id_certificado`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_profesores_certificados_profesor` FOREIGN KEY (`id_profesor`) REFERENCES `profesores` (`id_profesor`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `profesores_materias`;
CREATE TABLE `profesores_materias` (
  `id_profesor_materia` int unsigned NOT NULL AUTO_INCREMENT,
  `id_profesor` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  `ciclo_academico` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_profesor_materia`),
  UNIQUE KEY `uq_profesores_materias` (`id_profesor`,`id_materia`,`ciclo_academico`),
  KEY `fk_profesores_materias_materia` (`id_materia`),
  CONSTRAINT `fk_profesores_materias_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_profesores_materias_profesor` FOREIGN KEY (`id_profesor`) REFERENCES `profesores` (`id_profesor`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `publicaciones`;
CREATE TABLE `publicaciones` (
  `id_publicacion` int unsigned NOT NULL AUTO_INCREMENT,
  `id_estudiante` int unsigned NOT NULL,
  `id_carrera` int unsigned NOT NULL,
  `id_materia` int unsigned DEFAULT NULL,
  `titulo` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contenido` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_publicacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('abierta','resuelta','moderada') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'abierta',
  `id_respuesta_aceptada` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id_publicacion`),
  KEY `fk_publicaciones_estudiante` (`id_estudiante`),
  KEY `fk_publicaciones_respuesta_aceptada` (`id_respuesta_aceptada`),
  KEY `idx_publicaciones_carrera` (`id_carrera`),
  KEY `idx_publicaciones_materia` (`id_materia`),
  CONSTRAINT `fk_publicaciones_carrera` FOREIGN KEY (`id_carrera`) REFERENCES `carreras` (`id_carrera`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_publicaciones_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_publicaciones_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_publicaciones_respuesta_aceptada` FOREIGN KEY (`id_respuesta_aceptada`) REFERENCES `respuestas` (`id_respuesta`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `recursos_academicos`;
CREATE TABLE `recursos_academicos` (
  `id_recurso` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  `titulo` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `url_archivo` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `adjunto_nombre` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adjunto_tamano` bigint DEFAULT NULL,
  `tipo_archivo` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_subida` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `contador_descargas` int NOT NULL DEFAULT '0',
  `contador_reportes` int NOT NULL DEFAULT '0',
  `oculto` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_recurso`),
  KEY `fk_recursos_academicos_usuario` (`id_usuario`),
  KEY `idx_recursos_materia` (`id_materia`),
  CONSTRAINT `fk_recursos_academicos_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_recursos_academicos_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_recursos_academicos_auto_oculta` BEFORE UPDATE ON `recursos_academicos` FOR EACH ROW BEGIN
    IF NEW.contador_reportes >= 5 THEN
        SET NEW.oculto = TRUE;
    END IF;
END */;;
DELIMITER ;
DROP TABLE IF EXISTS `resenas_empresarial`;
CREATE TABLE `resenas_empresarial` (
  `id_resena` int unsigned NOT NULL AUTO_INCREMENT,
  `id_estudiante` int unsigned NOT NULL,
  `id_empresa` int unsigned NOT NULL,
  `calificacion` tinyint unsigned NOT NULL,
  `proyecto_desarrollado` text COLLATE utf8mb4_unicode_ci,
  `aprendizajes` text COLLATE utf8mb4_unicode_ci,
  `recomendaciones` text COLLATE utf8mb4_unicode_ci,
  `fecha_resena` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_resena`),
  KEY `fk_resenas_empresarial_estudiante` (`id_estudiante`),
  KEY `fk_resenas_empresarial_empresa` (`id_empresa`),
  CONSTRAINT `fk_resenas_empresarial_empresa` FOREIGN KEY (`id_empresa`) REFERENCES `empresas_vinculadas` (`id_empresa`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_resenas_empresarial_estudiante` FOREIGN KEY (`id_estudiante`) REFERENCES `estudiantes` (`id_estudiante`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_resenas_empresarial_calificacion` CHECK ((`calificacion` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_resenas_empresarial_semestre_minimo` BEFORE INSERT ON `resenas_empresarial` FOR EACH ROW BEGIN
    DECLARE v_semestre TINYINT;
    SELECT semestre_actual INTO v_semestre
    FROM estudiantes
    WHERE id_estudiante = NEW.id_estudiante;

    IF v_semestre IS NULL OR v_semestre < 6 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo estudiantes de semestre 6 en adelante pueden reseñar el Semestre Empresarial.';
    END IF;
END */;;
DELIMITER ;
DROP TABLE IF EXISTS `respuestas`;
CREATE TABLE `respuestas` (
  `id_respuesta` int unsigned NOT NULL AUTO_INCREMENT,
  `id_publicacion` int unsigned NOT NULL,
  `id_usuario` int unsigned NOT NULL,
  `contenido` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_respuesta` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `es_verificada_docente` tinyint(1) NOT NULL DEFAULT '0',
  `es_solucion` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_respuesta`),
  KEY `fk_respuestas_usuario` (`id_usuario`),
  KEY `idx_respuestas_publicacion` (`id_publicacion`),
  CONSTRAINT `fk_respuestas_publicacion` FOREIGN KEY (`id_publicacion`) REFERENCES `publicaciones` (`id_publicacion`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_respuestas_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_respuestas_verificacion_docente` BEFORE INSERT ON `respuestas` FOR EACH ROW BEGIN
    DECLARE v_rol VARCHAR(20);
    SELECT rol INTO v_rol FROM usuarios WHERE id_usuario = NEW.id_usuario;
    IF v_rol = 'profesor' THEN
        SET NEW.es_verificada_docente = TRUE;
    END IF;
END */;;
DELIMITER ;
DROP TABLE IF EXISTS `seguimientos`;
CREATE TABLE `seguimientos` (
  `id_seguimiento` int unsigned NOT NULL AUTO_INCREMENT,
  `id_seguidor` int unsigned NOT NULL,
  `id_seguido` int unsigned NOT NULL,
  `fecha_seguimiento` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_seguimiento`),
  UNIQUE KEY `uq_seguidor_seguido` (`id_seguidor`,`id_seguido`),
  KEY `fk_seg_seguido` (`id_seguido`),
  CONSTRAINT `fk_seg_seguido` FOREIGN KEY (`id_seguido`) REFERENCES `usuarios` (`id_usuario`),
  CONSTRAINT `fk_seg_seguidor` FOREIGN KEY (`id_seguidor`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `sesion_inscripciones`;
CREATE TABLE `sesion_inscripciones` (
  `id_sesion` int unsigned NOT NULL,
  `id_usuario` int unsigned NOT NULL,
  `fecha_inscripcion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_sesion`,`id_usuario`),
  KEY `fk_inscripcion_usuario` (`id_usuario`),
  CONSTRAINT `fk_inscripcion_sesion` FOREIGN KEY (`id_sesion`) REFERENCES `sesiones_repaso` (`id_sesion`) ON DELETE CASCADE,
  CONSTRAINT `fk_inscripcion_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `sesiones_repaso`;
CREATE TABLE `sesiones_repaso` (
  `id_sesion` int unsigned NOT NULL AUTO_INCREMENT,
  `titulo` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `materia` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `modalidad` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Grupal',
  `ubicacion` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha` date NOT NULL,
  `hora_inicio` time NOT NULL,
  `duracion_min` int NOT NULL DEFAULT '90',
  `cupo_max` int NOT NULL DEFAULT '30',
  `estado` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ABIERTA',
  `organizador_id` int unsigned NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_sesion`),
  KEY `idx_sesion_fecha` (`fecha`,`hora_inicio`),
  KEY `fk_sesion_organizador` (`organizador_id`),
  CONSTRAINT `fk_sesion_organizador` FOREIGN KEY (`organizador_id`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `tips_academicos`;
CREATE TABLE `tips_academicos` (
  `id_tip` int unsigned NOT NULL AUTO_INCREMENT,
  `id_usuario` int unsigned NOT NULL,
  `id_materia` int unsigned NOT NULL,
  `contenido` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_publicacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `total_votos` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_tip`),
  KEY `fk_tips_academicos_usuario` (`id_usuario`),
  KEY `idx_tips_materia` (`id_materia`),
  CONSTRAINT `fk_tips_academicos_materia` FOREIGN KEY (`id_materia`) REFERENCES `materias` (`id_materia`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tips_academicos_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `id_usuario` int unsigned NOT NULL AUTO_INCREMENT,
  `nombre_completo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contrasena_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `rol` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `puntos_reputacion` int NOT NULL DEFAULT '0',
  `foto_url` longtext COLLATE utf8mb4_unicode_ci,
  `fecha_registro` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuarios_correo` (`correo`),
  CONSTRAINT `chk_usuarios_correo_institucional` CHECK (((`correo` like _utf8mb4'%@tecmilenio.mx') or (`correo` like _utf8mb4'%@servicios.tecmilenio.mx')))
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS `votos_tips`;
CREATE TABLE `votos_tips` (
  `id_voto` int unsigned NOT NULL AUTO_INCREMENT,
  `id_tip` int unsigned NOT NULL,
  `id_usuario` int unsigned NOT NULL,
  `fecha_voto` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_voto`),
  UNIQUE KEY `uq_votos_tips` (`id_tip`,`id_usuario`),
  KEY `fk_votos_tips_usuario` (`id_usuario`),
  CONSTRAINT `fk_votos_tips_tip` FOREIGN KEY (`id_tip`) REFERENCES `tips_academicos` (`id_tip`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_votos_tips_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_votos_tips_incrementa` AFTER INSERT ON `votos_tips` FOR EACH ROW BEGIN
    UPDATE tips_academicos SET total_votos = total_votos + 1 WHERE id_tip = NEW.id_tip;
END */;;
DELIMITER ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=CURRENT_USER*/ /*!50003 TRIGGER `trg_votos_tips_decrementa` AFTER DELETE ON `votos_tips` FOR EACH ROW BEGIN
    UPDATE tips_academicos SET total_votos = total_votos - 1 WHERE id_tip = OLD.id_tip;
END */;;
DELIMITER ;


-- =====================================================================
-- 2) DATOS BASE (catálogos: carreras, materias, plan de estudios, empresas)
-- =====================================================================

LOCK TABLES `carreras` WRITE;
/*!40000 ALTER TABLE `carreras` DISABLE KEYS */;
INSERT INTO `carreras` (`id_carrera`, `nombre_carrera`, `clave_carrera`, `activa`) VALUES (3,'Ingeniería en Desarrollo de Software','ISSC',1),(4,'Ingeniería Industrial y de Sistemas','INDS',1),(5,'Ingeniería en Mecatrónica','IMTC',1),(6,'Licenciatura en Administración de Empresas','LADM',1),(7,'Licenciatura en Comercio Internacional','LCIN',1),(8,'Licenciatura en Mercadotecnia','LMKT',1),(9,'Licenciatura en Psicología','LPSI',1),(10,'Licenciatura en Derecho','LDRC',1);
/*!40000 ALTER TABLE `carreras` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `materias` WRITE;
/*!40000 ALTER TABLE `materias` DISABLE KEYS */;
INSERT INTO `materias` (`id_materia`, `nombre_materia`, `clave_materia`, `tipo_materia`, `creditos`) VALUES (3,'Fundamentos de programación','M0001','tronco_comun',5),(4,'Pensamiento lógico-matemático','M0002','tronco_comun',5),(5,'Habilidades de bienestar y liderazgo','M0003','bienestar',5),(6,'Comunicación efectiva','M0004','tronco_comun',5),(7,'Álgebra lineal','M0005','tronco_comun',5),(8,'Programación orientada a objetos (POO)','M0006','tronco_comun',5),(9,'Estructuras de datos lineales y no lineales','M0007','tronco_comun',5),(10,'Cálculo diferencial','M0008','tronco_comun',5),(11,'Fundamentos de redes y sistemas operativos','M0009','tronco_comun',5),(12,'Ética y ciudadanía','M0010','tronco_comun',5),(13,'Bases de datos relacionales y SQL','M0011','disciplinar',5),(14,'Algoritmos avanzados y complejidad','M0012','disciplinar',5),(15,'Cálculo integral y vectorial','M0013','disciplinar',5),(16,'Métodos numéricos','M0014','disciplinar',5),(17,'Arquitectura y diseño de software','M0015','disciplinar',5),(18,'Desarrollo web Front-End (HTML/CSS/JS/React)','M0016','disciplinar',5),(19,'Programación Back-End y APIs REST','M0017','disciplinar',5),(20,'Bases de datos NoSQL y Big Data','M0018','disciplinar',5),(21,'Metodologías ágiles de desarrollo (Scrum/DevOps)','M0019','disciplinar',5),(22,'Pruebas y calidad de software (QA)','M0020','disciplinar',5),(23,'Seguridad informática y criptografía','M0021','disciplinar',5),(24,'Semestre Empresarial: Proyecto de desarrollo de software en empresa','M0022','disciplinar',5),(25,'Práctica profesional de TI','M0023','disciplinar',5),(26,'Evaluación y entrega de entregables organizacionales','M0024','disciplinar',5),(27,'Computación en la nube (Cloud Architecture)','M0025','disciplinar',5),(28,'Integración y despliegue continuo (CI/CD)','M0026','disciplinar',5),(29,'Arquitectura de microservicios','M0027','disciplinar',5),(30,'Proyecto integrador capstone de software','M0028','disciplinar',5),(31,'Inteligencia artificial empresarial','M0029','disciplinar',5),(32,'Gestión y gobierno de proyectos de TI','M0030','disciplinar',5),(33,'Introducción a la ingeniería industrial','M0031','tronco_comun',5),(34,'Química industrial','M0032','tronco_comun',5),(35,'Habilidades de bienestar','M0033','bienestar',5),(36,'Dibujo industrial y CAD','M0034','tronco_comun',5),(37,'Cálculo integral','M0035','tronco_comun',5),(38,'Física mecánica','M0036','tronco_comun',5),(39,'Estudio del trabajo y tiempos/movimientos','M0037','tronco_comun',5),(40,'Probabilidad y estadística aplicada','M0038','disciplinar',5),(41,'Estática y resistencia de materiales','M0039','disciplinar',5),(42,'Ergonomía y diseño de estaciones','M0040','disciplinar',5),(43,'Termofluidos','M0041','disciplinar',5),(44,'Control estadístico de la calidad','M0042','disciplinar',5),(45,'Procesos de manufactura y materiales','M0043','disciplinar',5),(46,'Contabilidad de costos industriales','M0044','disciplinar',5),(47,'Planeación y control de la producción (PCP)','M0045','disciplinar',5),(48,'Seguridad e higiene industrial','M0046','disciplinar',5),(49,'Simulación de procesos industriales','M0047','disciplinar',5),(50,'Semestre Empresarial: Proyecto de optimización de planta','M0048','disciplinar',5),(51,'Implementación de mejora continua','M0049','disciplinar',5),(52,'Reporte de impacto operativo','M0050','disciplinar',5),(53,'Logística y cadena de suministro','M0051','disciplinar',5),(54,'Formulación y evaluación de proyectos de inversión','M0052','disciplinar',5),(55,'Mantenimiento industrial y TPM','M0053','disciplinar',5),(56,'Dirección de operaciones','M0054','disciplinar',5),(57,'Proyecto integrador industrial capstone','M0055','disciplinar',5),(58,'Normatividad y auditorías de calidad ISO','M0056','disciplinar',5),(59,'Introducción a la mecatrónica','M0057','tronco_comun',5),(60,'Liderazgo y bienestar','M0058','bienestar',5),(61,'Electricidad y magnetismo','M0059','tronco_comun',5),(62,'Estática y dinámica','M0060','tronco_comun',5),(63,'Algoritmos computacionales para ingeniería','M0061','tronco_comun',5),(64,'Ecuaciones diferenciales','M0062','disciplinar',5),(65,'Circuitos eléctricos','M0063','disciplinar',5),(66,'Mecánica de materiales y mecanismos','M0064','disciplinar',5),(67,'Diseño CAD avanzado','M0065','disciplinar',5),(68,'Electrónica analógica y de potencia','M0066','disciplinar',5),(69,'Termodinámica y fluidos','M0067','disciplinar',5),(70,'Sensores e instrumentación industrial','M0068','disciplinar',5),(71,'Electrónica digital y microcontroladores','M0069','disciplinar',5),(72,'Controladores lógicos programables (PLC)','M0070','disciplinar',5),(73,'Sistemas de control clásico y moderno','M0071','disciplinar',5),(74,'Semestre Empresarial: Proyecto de automatización mecatrónica en empresa','M0072','disciplinar',5),(75,'Mantenimiento e instrumentación aplicada','M0073','disciplinar',5),(76,'Robótica industrial y cinemática','M0074','disciplinar',5),(77,'Sistemas hidráulicos y neumáticos','M0075','disciplinar',5),(78,'Redes de comunicación industrial','M0076','disciplinar',5),(79,'Proyecto de integración mecatrónica capstone','M0077','disciplinar',5),(80,'Automatización integrada por computadora (CIM)','M0078','disciplinar',5),(81,'Control predictivo','M0079','disciplinar',5),(82,'Fundamentos de administración','M0080','tronco_comun',5),(83,'Matemáticas aplicadas a negocios','M0081','tronco_comun',5),(84,'Fundamentos de mercadotecnia','M0082','tronco_comun',5),(85,'Contabilidad financiera','M0083','tronco_comun',5),(86,'Microeconomía y conducta de mercado','M0084','tronco_comun',5),(87,'Derecho corporativo','M0085','tronco_comun',5),(88,'Habilidades de comunicación y negociación','M0086','tronco_comun',5),(89,'Contabilidad de costos y presupuestos','M0087','disciplinar',5),(90,'Macroeconomía y entorno global','M0088','disciplinar',5),(91,'Estadística descriptiva de negocios','M0089','disciplinar',5),(92,'Derecho laboral','M0090','disciplinar',5),(93,'Comportamiento organizacional','M0091','disciplinar',5),(94,'Matemáticas financieras','M0092','disciplinar',5),(95,'Administración de compras y abastecimiento','M0093','disciplinar',5),(96,'Gestión del talento humano y nóminas','M0094','disciplinar',5),(97,'Finanzas corporativas básicas','M0095','disciplinar',5),(98,'Estrategias de modelos de negocio','M0096','disciplinar',5),(99,'Semestre Empresarial: Diagnóstico y propuesta de optimización operativa','M0097','disciplinar',5),(100,'Ejecución de proyecto corporativo','M0098','disciplinar',5),(101,'Planeación estratégica y control de gestión','M0099','disciplinar',5),(102,'Administración de operaciones y servicios','M0100','disciplinar',5),(103,'Responsabilidad social corporativa','M0101','disciplinar',5),(104,'Dirección general y toma de decisiones','M0102','disciplinar',5),(105,'Proyecto integrador de negocios (Simulación ejecutiva)','M0103','disciplinar',5),(106,'Consultoría empresarial','M0104','disciplinar',5),(107,'Introducción al comercio exterior','M0105','tronco_comun',5),(108,'Matemáticas para negocios','M0106','tronco_comun',5),(109,'Bienestar y liderazgo','M0107','bienestar',5),(110,'Geografía económica y geopolítica','M0108','tronco_comun',5),(111,'Microeconomía','M0109','tronco_comun',5),(112,'Comunicación intercultural','M0110','tronco_comun',5),(113,'Legislación y derecho aduanero mexicano','M0111','disciplinar',5),(114,'Macroeconomía','M0112','disciplinar',5),(115,'Estadística aplicada al comercio','M0113','disciplinar',5),(116,'Contratos internacionales','M0114','disciplinar',5),(117,'Clasificación arancelaria y merceología','M0115','disciplinar',5),(118,'Finanzas internacionales y tipo de cambio','M0116','disciplinar',5),(119,'Tratados de libre comercio (T-MEC y globales)','M0117','disciplinar',5),(120,'Logística internacional y transporte multimodal','M0118','disciplinar',5),(121,'Envase, embalaje y normativas de exportación','M0119','disciplinar',5),(122,'Mercadotecnia internacional','M0120','disciplinar',5),(123,'Semestre Empresarial: Proyecto de importación/exportación o cadena aduanal en empresa','M0121','disciplinar',5),(124,'Auditoría operativa de comercio','M0122','disciplinar',5),(125,'Programas de fomento al comercio (IMMEX, PROSEC)','M0123','disciplinar',5),(126,'Formulación de planes de exportación','M0124','disciplinar',5),(127,'Gestión portuaria y aduanera','M0125','disciplinar',5),(128,'Negociación y diplomacia comercial','M0126','disciplinar',5),(129,'Proyecto integrador de internacionalización','M0127','disciplinar',5),(130,'Arbitraje comercial internacional','M0128','disciplinar',5),(131,'Comportamiento del consumidor','M0129','tronco_comun',5),(132,'Creatividad e innovación publicitaria','M0130','tronco_comun',5),(133,'Contabilidad de mercadotecnia','M0131','tronco_comun',5),(134,'Microeconomía y precios','M0132','tronco_comun',5),(135,'Comunicación persuasiva','M0133','tronco_comun',5),(136,'Investigación cualitativa y cuantitativa de mercados','M0134','disciplinar',5),(137,'Estadística inferencial','M0135','disciplinar',5),(138,'Fundamentos de diseño gráfico publicitario','M0136','disciplinar',5),(139,'Estrategia de producto y branding','M0137','disciplinar',5),(140,'Marketing digital y redes sociales','M0138','disciplinar',5),(141,'Fijación de precios y rentabilidad','M0139','disciplinar',5),(142,'Analítica web y métricas de marketing (KPIs)','M0140','disciplinar',5),(143,'Canales de distribución y trade marketing','M0141','disciplinar',5),(144,'Campañas de comunicación integrada','M0142','disciplinar',5),(145,'Semestre Empresarial: Ejecución de estrategia de marketing o lanzamiento de campaña en empresa aliada','M0143','disciplinar',5),(146,'Mercadotecnia de servicios y experiencia del cliente (CX)','M0144','disciplinar',5),(147,'E-Commerce y embudos de venta','M0145','disciplinar',5),(148,'Neuromarketing','M0146','disciplinar',5),(149,'Plan maestro de mercadotecnia integrador','M0147','disciplinar',5),(150,'Dirección estratégica de marcas','M0148','disciplinar',5),(151,'Ética y legislación publicitaria','M0149','disciplinar',5),(152,'Introducción a la psicología','M0150','tronco_comun',5),(153,'Bases biológicas de la conducta humana','M0151','tronco_comun',5),(154,'Historia de la psicología','M0152','tronco_comun',5),(155,'Neuroanatomía y fisiología sensorial','M0153','tronco_comun',5),(156,'Psicología del desarrollo infantil','M0154','tronco_comun',5),(157,'Teorías de la personalidad','M0155','tronco_comun',5),(158,'Epistemología psicológica','M0156','tronco_comun',5),(159,'Psicología del desarrollo adolescente y adulto','M0157','disciplinar',5),(160,'Procesos cognitivos y del aprendizaje','M0158','disciplinar',5),(161,'Estadística aplicada a ciencias sociales','M0159','disciplinar',5),(162,'Psicometría y medición del comportamiento','M0160','disciplinar',5),(163,'Psicopatología general','M0161','disciplinar',5),(164,'Métodos de entrevista psicológica','M0162','disciplinar',5),(165,'Psicología social y comunitaria','M0163','disciplinar',5),(166,'Evaluación psicodiagnóstica','M0164','disciplinar',5),(167,'Modelos de intervención terapéutica y preventiva','M0165','disciplinar',5),(168,'Semestre Empresarial / Práctica Institucional: Intervención en área de recursos humanos, clínica o educativa en convenio','M0166','disciplinar',5),(169,'Psicología organizacional y clima laboral','M0167','disciplinar',5),(170,'Intervención en crisis','M0168','disciplinar',5),(171,'Técnicas de consejería y bienestar','M0169','bienestar',5),(172,'Ética profesional en psicología','M0170','disciplinar',5),(173,'Seminario integrador de diagnóstico e intervención','M0171','disciplinar',5),(174,'Taller de psicoterapia humanista/positiva','M0172','disciplinar',5),(175,'Introducción al estudio del derecho','M0173','tronco_comun',5),(176,'Derecho romano y sistemas jurídicos','M0174','tronco_comun',5),(177,'Historia del derecho en México','M0175','tronco_comun',5),(178,'Bienestar','M0176','bienestar',5),(179,'Teoría general del derecho y del Estado','M0177','tronco_comun',5),(180,'Derecho civil (Personas y familia)','M0178','tronco_comun',5),(181,'Derecho constitucional mexicano','M0179','tronco_comun',5),(182,'Argumentación','M0180','tronco_comun',5),(183,'Derecho civil (Bienes y sucesiones)','M0181','disciplinar',5),(184,'Derecho penal general (Delitos y penas)','M0182','disciplinar',5),(185,'Derechos humanos y garantías individuales','M0183','disciplinar',5),(186,'Derecho civil (Obligaciones y contratos)','M0184','disciplinar',5),(187,'Derecho procesal penal y juicios orales','M0185','disciplinar',5),(188,'Derecho mercantil y títulos de crédito','M0186','disciplinar',5),(189,'Juicio de amparo I','M0187','disciplinar',5),(190,'Sociedades mercantiles y contratos corporativos','M0188','disciplinar',5),(191,'Derecho individual y colectivo del trabajo','M0189','disciplinar',5),(192,'Semestre Empresarial / Práctica Jurídica: Práctica legal en despacho, corporativo, juzgado o dependencia pública en convenio','M0190','disciplinar',5),(193,'Juicio de amparo II','M0191','disciplinar',5),(194,'Derecho administrativo y fiscal','M0192','disciplinar',5),(195,'Métodos alternos de solución de controversias (MASC)','M0193','disciplinar',5),(196,'Seminario de litigio estratégico integrador','M0194','disciplinar',5),(197,'Derecho internacional público y privado','M0195','disciplinar',5),(198,'Deontología y ética jurídica','M0196','disciplinar',5);
/*!40000 ALTER TABLE `materias` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `plan_estudios` WRITE;
/*!40000 ALTER TABLE `plan_estudios` DISABLE KEYS */;
INSERT INTO `plan_estudios` (`id_plan_estudios`, `id_carrera`, `id_materia`, `semestre_sugerido`) VALUES (312,3,3,1),(313,3,4,1),(314,3,5,1),(315,3,6,1),(316,3,7,1),(317,3,8,2),(318,3,9,2),(319,3,10,2),(320,3,11,2),(321,3,12,2),(322,3,13,3),(323,3,14,3),(324,3,15,3),(325,3,16,3),(326,3,17,3),(327,3,18,4),(328,3,19,4),(329,3,20,4),(330,3,21,5),(331,3,22,5),(332,3,23,5),(333,3,24,6),(334,3,25,6),(335,3,26,6),(336,3,27,7),(337,3,28,7),(338,3,29,7),(339,3,30,8),(340,3,31,8),(341,3,32,8),(342,4,10,1),(343,4,33,1),(344,4,34,1),(345,4,35,1),(346,4,36,1),(347,4,6,2),(348,4,7,2),(349,4,37,2),(350,4,38,2),(351,4,39,2),(352,4,40,3),(353,4,41,3),(354,4,42,3),(355,4,43,3),(356,4,44,4),(357,4,45,4),(358,4,46,4),(359,4,47,5),(360,4,48,5),(361,4,49,5),(362,4,50,6),(363,4,51,6),(364,4,52,6),(365,4,53,7),(366,4,54,7),(367,4,55,7),(368,4,56,8),(369,4,57,8),(370,4,58,8),(371,5,3,1),(372,5,7,1),(373,5,10,1),(374,5,59,1),(375,5,60,1),(376,5,37,2),(377,5,61,2),(378,5,62,2),(379,5,63,2),(380,5,64,3),(381,5,65,3),(382,5,66,3),(383,5,67,3),(384,5,68,4),(385,5,69,4),(386,5,70,4),(387,5,71,5),(388,5,72,5),(389,5,73,5),(390,5,74,6),(391,5,75,6),(392,5,76,7),(393,5,77,7),(394,5,78,7),(395,5,79,8),(396,5,80,8),(397,5,81,8),(398,6,35,1),(399,6,82,1),(400,6,83,1),(401,6,84,1),(402,6,85,2),(403,6,86,2),(404,6,87,2),(405,6,88,2),(406,6,89,3),(407,6,90,3),(408,6,91,3),(409,6,92,3),(410,6,93,4),(411,6,94,4),(412,6,95,4),(413,6,96,5),(414,6,97,5),(415,6,98,5),(416,6,99,6),(417,6,100,6),(418,6,101,7),(419,6,102,7),(420,6,103,7),(421,6,104,8),(422,6,105,8),(423,6,106,8),(424,7,84,1),(425,7,107,1),(426,7,108,1),(427,7,109,1),(428,7,85,2),(429,7,110,2),(430,7,111,2),(431,7,112,2),(432,7,113,3),(433,7,114,3),(434,7,115,3),(435,7,116,3),(436,7,117,4),(437,7,118,4),(438,7,119,4),(439,7,120,5),(440,7,121,5),(441,7,122,5),(442,7,123,6),(443,7,124,6),(444,7,125,7),(445,7,126,7),(446,7,127,7),(447,7,128,8),(448,7,129,8),(449,7,130,8),(450,8,60,1),(451,8,84,1),(452,8,108,1),(453,8,131,1),(454,8,132,2),(455,8,133,2),(456,8,134,2),(457,8,135,2),(458,8,136,3),(459,8,137,3),(460,8,138,3),(461,8,139,4),(462,8,140,4),(463,8,141,4),(464,8,142,5),(465,8,143,5),(466,8,144,5),(467,8,145,6),(468,8,146,7),(469,8,147,7),(470,8,148,7),(471,8,149,8),(472,8,150,8),(473,8,151,8),(474,9,109,1),(475,9,152,1),(476,9,153,1),(477,9,154,1),(478,9,155,2),(479,9,156,2),(480,9,157,2),(481,9,158,2),(482,9,159,3),(483,9,160,3),(484,9,161,3),(485,9,162,4),(486,9,163,4),(487,9,164,4),(488,9,165,5),(489,9,166,5),(490,9,167,5),(491,9,168,6),(492,9,169,7),(493,9,170,7),(494,9,171,7),(495,9,172,8),(496,9,173,8),(497,9,174,8),(498,10,175,1),(499,10,176,1),(500,10,177,1),(501,10,178,1),(502,10,179,2),(503,10,180,2),(504,10,181,2),(505,10,182,2),(506,10,183,3),(507,10,184,3),(508,10,185,3),(509,10,186,4),(510,10,187,4),(511,10,188,4),(512,10,189,5),(513,10,190,5),(514,10,191,5),(515,10,192,6),(516,10,193,7),(517,10,194,7),(518,10,195,7),(519,10,196,8),(520,10,197,8),(521,10,198,8);
/*!40000 ALTER TABLE `plan_estudios` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `empresas_vinculadas` WRITE;
/*!40000 ALTER TABLE `empresas_vinculadas` DISABLE KEYS */;
INSERT INTO `empresas_vinculadas` (`id_empresa`, `nombre_empresa`, `sector`, `sitio_web`, `descripcion`, `carreras_afines`) VALUES (91,'IBM','Tecnología','https://www.ibm.com','Empresa multinacional de tecnología y consultoría.','Ingeniería en Sistemas, Ingeniería Industrial'),(92,'Tata Consultancy Services (TCS)','Tecnología y Consultoría','https://www.tcs.com','Consultora multinacional de servicios de TI.','Ingeniería en Sistemas, Administración'),(93,'Softtek','Tecnología y Consultoría','https://www.softtek.com','Proveedor global de soluciones de TI.','Ingeniería en Sistemas, Ingeniería Industrial'),(94,'Infosys','Tecnología y Consultoría','https://www.infosys.com','Consultora global de servicios de tecnología.','Ingeniería en Sistemas, Mercadotecnia'),(95,'HubSpot','Software','https://www.hubspot.com','Plataforma de CRM y marketing digital.','Mercadotecnia, Administración'),(96,'Google','Tecnología','https://www.google.com','Empresa de tecnología y servicios digitales.','Ingeniería en Sistemas, Mercadotecnia'),(97,'Intel','Tecnología','https://www.intel.com','Fabricante de semiconductores y procesadores.','Ingeniería en Sistemas, Ingeniería en Mecatrónica'),(98,'Microsoft','Tecnología','https://www.microsoft.com','Empresa multinacional de software y servicios.','Ingeniería en Sistemas, Mercadotecnia'),(99,'Foxconn','Tecnología, Software y Telecomunicaciones','https://www.foxconn.com','Fabricación y ensamble por contrato de dispositivos electrónicos y hardware de computación.','Ing. Industrial, Ing. Mecatrónica, Logística y Cadena de Suministro'),(100,'Flextronics (Flex)','Tecnología, Software y Telecomunicaciones','https://www.flex.com','Diseño, manufactura avanzada y ensamble de componentes electrónicos y dispositivos IoT.','Ing. Mecatrónica, Ing. Industrial, Comercio Internacional, Logística'),(101,'Sanmina','Tecnología, Software y Telecomunicaciones','https://www.sanmina.com','Fabricación de circuitos impresos y productos electrónicos de alta precisión (médico, aeroespacial).','Ing. Industrial, Ing. Mecatrónica, Administración de Empresas'),(102,'TCA Software Solutions','Tecnología, Software y Telecomunicaciones','','Desarrollo de software de punto de venta (POS) y soluciones comerciales para retail.','Ing. en Software, Ing. en Sistemas, Mercadotecnia'),(103,'Adient','Automotriz, Aeroespacial y Manufactura Avanzada','','Fabricación y ensamble de asientos y estructuras interiores para la industria automotriz.','Ing. Industrial, Ing. Mecatrónica, Logística'),(104,'Continental','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.continental.com','Producción de componentes automotrices, neumáticos y sistemas de conectividad para vehículos.','Ing. Mecatrónica, Ing. Industrial, Ing. en Software'),(105,'Faurecia','Automotriz, Aeroespacial y Manufactura Avanzada','','Desarrollo de tecnología automotriz, cabinas inteligentes y soluciones de movilidad limpia.','Ing. Mecatrónica, Ing. Industrial, Diseño y Desarrollo Digital'),(106,'FCA / Stellantis','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.stellantis.com','Ensamble y manufactura de vehículos, motores y componentes automotrices (Chrysler, Jeep, Ram).','Ing. Mecatrónica, Ing. Industrial, Administración, Logística'),(107,'Ford Motor Company','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.ford.com','Diseño, manufactura y ensamblaje de automóviles, camiones y componentes mecánicos.','Ing. Mecatrónica, Ing. Industrial, Finanzas, Logística'),(108,'General Electric (GE)','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.ge.com','Fabricación de turbinas, sistemas de energía, tecnología aeroespacial y equipo industrial.','Ing. Industrial, Ing. Mecatrónica, Finanzas, Negocios'),(109,'Hella','Automotriz, Aeroespacial y Manufactura Avanzada','','Desarrollo y fabricación de sistemas de iluminación y electrónica automotriz.','Ing. Mecatrónica, Ing. Industrial, Ing. en Sistemas'),(110,'Honda','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.honda.com','Ensamble de automóviles, motocicletas y componentes de combustión y potencia.','Ing. Mecatrónica, Ing. Industrial, Comercio Internacional'),(111,'Hyundai','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.hyundai.com','Fabricación de automóviles, autopartes y maquinaria para transporte.','Ing. Industrial, Ing. Mecatrónica, Mercadotecnia, Finanzas'),(112,'John Deere','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.deere.com','Fabricación de maquinaria agrícola, motores diésel y equipo de construcción.','Ing. Mecatrónica, Ing. Industrial, Logística'),(113,'Magna International','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.magna.com','Suministro integral de autopartes, sistemas de chasis, transmisiones y espejos automotrices.','Ing. Mecatrónica, Ing. Industrial, Administración, Logística'),(114,'MAHLE','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.mahle.com','Producción de componentes para motores, filtración y gestión térmica en vehículos.','Ing. Mecatrónica, Ing. Industrial'),(115,'Metalsa','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.metalsa.com','Fabricación de estructuras metálicas, largueros y chasis para vehículos ligeros y pesados.','Ing. Industrial, Ing. Mecatrónica, Logística'),(116,'Nemak','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.nemak.com','Fundición y maquinado de componentes de aluminio para motor, transmisión y autos eléctricos.','Ing. Industrial, Ing. Mecatrónica, Comercio Internacional'),(117,'Robert Bosch','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.bosch.com','Soluciones de ingeniería, electrónica automotriz, herramientas eléctricas y automatización industrial.','Ing. Mecatrónica, Ing. en Software, Ing. Industrial'),(118,'Rockwell Automation','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.rockwellautomation.com','Automatización industrial, control digital, robótica y software de manufactura.','Ing. Mecatrónica, Ing. en Sistemas, Ing. Industrial'),(119,'Safran Group','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.safran-group.com','Diseño, manufactura y mantenimiento de motores de avión, cableado e interiores aeroespaciales.','Ing. Mecatrónica, Ing. Industrial, Logística'),(120,'Schneider Electric','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.se.com','Soluciones digitales de gestión de energía, cuadros eléctricos y automatización de edificios.','Ing. Mecatrónica, Ing. Industrial, Ing. en Software'),(121,'Siemens','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.siemens.com','Infraestructura inteligente, electrificación, software industrial y automatización de plantas.','Ing. en Software, Ing. Mecatrónica, Ing. Industrial'),(122,'Suzuki Vallejo','Automotriz, Aeroespacial y Manufactura Avanzada','','Distribución, comercialización y servicio postventa de automóviles y motocicletas.','Administración, Mercadotecnia, Comercio Internacional'),(123,'TE Connectivity','Automotriz, Aeroespacial y Manufactura Avanzada','https://www.te.com','Conectores y sensores para aplicaciones industriales, automotrices y de telecomunicaciones.','Ing. Mecatrónica, Ing. Industrial'),(124,'TechOps México','Automotriz, Aeroespacial y Manufactura Avanzada','','Mantenimiento, reparación y revisión mayor (MRO) de aeronaves comerciales.','Ing. Mecatrónica, Ing. Industrial, Logística'),(125,'Air Liquide','Materiales, Energía, Construcción y Metalmecánica','https://www.airliquide.com','Producción y distribución de gases industriales, medicinales y ambientales.','Ing. Industrial, Logística, Administración'),(126,'CEMEX','Materiales, Energía, Construcción y Metalmecánica','https://www.cemex.com','Producción, distribución y venta de cemento, concreto premezclado y agregados.','Ing. Industrial, Administración de Empresas, Finanzas, Logística'),(127,'Cuprum','Materiales, Energía, Construcción y Metalmecánica','','Extrusión y transformación de perfiles de aluminio y escaleras industriales.','Ing. Industrial, Comercio Internacional, Mercadotecnia'),(128,'Gas Natural (Naturgy)','Materiales, Energía, Construcción y Metalmecánica','https://www.naturgy.com','Distribución y comercialización de gas natural y soluciones de energía.','Ing. Industrial, Finanzas, Administración'),(129,'GCC (Cementos de Chihuahua)','Materiales, Energía, Construcción y Metalmecánica','https://www.gcc.com','Producción de cemento, concreto y soluciones para la construcción pesada.','Ing. Industrial, Finanzas, Logística'),(130,'Holcim Apasco','Materiales, Energía, Construcción y Metalmecánica','https://www.holcim.com','Soluciones para la construcción, producción de cemento y agregados sostenibles.','Ing. Industrial, Administración, Finanzas'),(131,'Industrias Rheem','Materiales, Energía, Construcción y Metalmecánica','https://www.rheem.com','Fabricación de calentadores de agua, calderas y equipos de aire acondicionado.','Ing. Industrial, Ing. Mecatrónica, Mercadotecnia'),(132,'PPG','Materiales, Energía, Construcción y Metalmecánica','https://www.ppg.com','Fabricación de pinturas, recubrimientos industriales y materiales especializados.','Ing. Industrial, Comercio Internacional, Mercadotecnia'),(133,'Reynera','Materiales, Energía, Construcción y Metalmecánica','','Fabricación de artículos de limpieza plásticos y herramientas de mantenimiento.','Ing. Industrial, Mercadotecnia, Logística'),(134,'Semex','Materiales, Energía, Construcción y Metalmecánica','','Soluciones de señalización vial, control de tráfico y sistemas de transporte inteligente.','Ing. Mecatrónica, Ing. en Sistemas, Ing. Industrial'),(135,'Ternium','Materiales, Energía, Construcción y Metalmecánica','https://www.ternium.com','Producción y procesamiento de productos de acero plano y largo para la industria.','Ing. Industrial, Ing. Mecatrónica, Finanzas, Logística'),(136,'Whirlpool','Materiales, Energía, Construcción y Metalmecánica','https://www.whirlpool.com','Fabricación y ensamble de electrodomésticos para el hogar e industriales.','Ing. Mecatrónica, Ing. Industrial, Diseño Gráfico, Mercadotecnia'),(137,'Adelnor','Consumo Masivo, Alimentos, Bebidas y Agronegocios','','Distribución de insumos agrícolas, fertilizantes y soluciones para el campo.','Administración, Comercio Internacional, Mercadotecnia'),(138,'FEMSA','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.femsa.com','Embotellado de bebidas (Coca-Cola FEMSA), retail (OXXO), logística y servicios digitales.','Administración, Finanzas, Mercadotecnia, Logística, Ing. en Sistemas'),(139,'Grupo Altex','Consumo Masivo, Alimentos, Bebidas y Agronegocios','','Procesamiento y exportación de frutas, verduras congeladas y harinas.','Ing. Industrial, Comercio Internacional, Nutrición'),(140,'Grupo Bafar','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.grupobafar.com','Producción, procesamiento y comercialización de embutidos, carnes frías y alimentos.','Ing. Industrial, Nutrición, Mercadotecnia, Finanzas'),(141,'Grupo Bimbo','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.grupobimbo.com','Panificación a gran escala, producción de botanas y distribución alimentaria.','Ing. Industrial, Nutrición, Logística, Mercadotecnia, Finanzas'),(142,'Grupo Bocar','Consumo Masivo, Alimentos, Bebidas y Agronegocios','','Manufactura de componentes de alta precisión en aluminio y plástico para la industria.','Ing. Industrial, Ing. Mecatrónica'),(143,'Heineken México','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.heinekenmexico.com','Elaboración, distribución y comercialización de marcas de cerveza y bebidas.','Ing. Industrial, Mercadotecnia, Logística, Administración, Finanzas'),(144,'Herbalife','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.herbalife.com','Comercialización de suplementos alimenticios, nutrición y cuidado personal por venta directa.','Nutrición, Mercadotecnia, Administración'),(145,'Hershey México','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.hershey.com','Producción de chocolates, confitería y productos derivados del cacao.','Ing. Industrial, Mercadotecnia, Logística, Finanzas'),(146,'PepsiCo','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.pepsico.com','Elaboración y distribución global de botanas (Sabritas, Gamesa) y bebidas.','Ing. Industrial, Mercadotecnia, Finanzas, Logística, Nutrición'),(147,'Pesca Azteca','Consumo Masivo, Alimentos, Bebidas y Agronegocios','','Captura, congelación y procesamiento comercial de atún a nivel industrial.','Ing. Industrial, Comercio Internacional, Logística'),(148,'Philip Morris Internacional','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.pmi.com','Manufactura y distribución de productos de tabaco y alternativas libres de humo.','Mercadotecnia, Administración, Finanzas, Ing. Industrial'),(149,'Pilgrim\'s Pride','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.pilgrims.com','Procesamiento avícola, empaque y distribución de productos cárnicos frescos y congelados.','Ing. Industrial, Nutrición, Logística'),(150,'Pochteca','Consumo Masivo, Alimentos, Bebidas y Agronegocios','https://www.pochteca.net','Distribución de materias primas químicas, papel, cartón y lubricantes industriales.','Ing. Industrial, Comercio Internacional, Administración'),(151,'7-Eleven','Retail, Comercio, Logística y Servicios','https://www.7-eleven.com.mx','Cadena de tiendas de conveniencia y comercialización de alimentos rápidos y servicios.','Administración, Mercadotecnia, Finanzas, Logística'),(152,'Casa Ley','Retail, Comercio, Logística y Servicios','https://www.casaley.com','Cadena de supermercados, tiendas de autoservicio y distribución de abarrotes.','Administración, Logística, Mercadotecnia, Contabilidad'),(153,'Fastenal','Retail, Comercio, Logística y Servicios','https://www.fastenal.com.mx','Suministro de fijaciones mecánicas, herramientas y gestión de inventarios para plantas.','Logística y Cadena de Suministro, Comercio Internacional, Ing. Industrial'),(154,'FirstCall Seguridad Privada','Retail, Comercio, Logística y Servicios','','Servicios de vigilancia intramuros, monitoreo y consultoría de seguridad patrimonial.','Derecho, Psicología, Administración'),(155,'GAP (Grupo Aeroportuario del Pacífico)','Retail, Comercio, Logística y Servicios','https://www.aeropuertosgap.com.mx','Operación, infraestructura y administración de aeropuertos internacionales en México.','Administración, Logística, Finanzas, Comercio Internacional'),(156,'Geni de México','Retail, Comercio, Logística y Servicios','','Servicios de comercialización y distribución de artículos especializados.','Comercio Internacional, Administración'),(157,'Grupo BAL','Retail, Comercio, Logística y Servicios','','Conglomerado diversificado con operaciones en retail (El Palacio de Hierro), minería y seguros.','Finanzas, Mercadotecnia, Administración, Derecho'),(158,'Grupo Caliente','Retail, Comercio, Logística y Servicios','','Entretenimiento, casas de apuestas, hotelería y gestión de eventos deportivos.','Administración, Mercadotecnia, Ing. en Software, Psicología'),(159,'Grupo Carso','Retail, Comercio, Logística y Servicios','https://www.gcarso.com.mx','Conglomerado industrial, comercial (Sanborns, Sears) y de infraestructura.','Ing. Industrial, Finanzas, Administración, Ing. en Software'),(160,'Grupo Dicas','Retail, Comercio, Logística y Servicios','','Distribución automotriz, logística y arrendamiento de equipo en el sureste de México.','Administración, Mercadotecnia, Finanzas'),(161,'Home Depot México','Retail, Comercio, Logística y Servicios','https://www.homedepot.com.mx','Tiendas minoristas de mejoras para el hogar, materiales de construcción y ferretería.','Administración, Mercadotecnia, Logística, Ing. Industrial'),(162,'Price Shoes Corporativo','Retail, Comercio, Logística y Servicios','','Venta por catálogo y retail de calzado, ropa y accesorios de moda.','Mercadotecnia, Diseño Gráfico, Administración, Logística'),(163,'Ryder de México','Retail, Comercio, Logística y Servicios','https://www.ryder.com','Gestión integral de cadenas de suministro, logística de transporte y almacenamiento.','Logística y Cadena de Suministro, Ing. Industrial, Comercio Internacional'),(164,'S-Mart','Retail, Comercio, Logística y Servicios','','Cadena de tiendas de autoservicio y supermercados de operación 24 horas.','Administración, Logística, Contabilidad, Mercadotecnia'),(165,'BBVA México','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.bbva.mx','Banca múltiple, servicios financieros corporativos, créditos, inversiones y seguros.','Finanzas, Administración, Ing. en Software (Fintech), Derecho'),(166,'Financiera Independencia','Finanzas, Seguros y Servicios Legales/Prendarios','','Otorgamiento de microcréditos personales y préstamos para sectores populares.','Finanzas, Administración, Mercadotecnia, Psicología'),(167,'First Cash','Finanzas, Seguros y Servicios Legales/Prendarios','','Casas de empeño, préstamos prendarios y venta de mercancía seminueva.','Administración, Finanzas, Contabilidad'),(168,'Fundación Dondé','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.educacion.donde.com.mx','Servicios de empeño social, préstamos garantizados y proyectos educativos de filantropía.','Finanzas, Administración, Psicología, Derecho'),(169,'Scotiabank','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.scotiabank.com.mx','Servicios bancarios minoristas, comerciales, banca corporativa y gestión de patrimonios.','Finanzas, Administración, Comercio Internacional, Ing. en Software'),(170,'Seguros MAPFRE','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.mapfre.com.mx','Coberturas de seguros de vida, autos, gastos médicos y fianzas empresariales.','Finanzas, Administración, Derecho, Mercadotecnia'),(171,'Seguros Monterrey New York Life','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.monterreynewyorklife.com','Pólizas de seguros de vida, retiro y gastos médicos mayores.','Finanzas, Administración, Psicología, Mercadotecnia'),(172,'SURA','Finanzas, Seguros y Servicios Legales/Prendarios','https://www.sura.com','Gestión de fondos de ahorro para el retiro (Afore), pensiones y fondos de inversión.','Finanzas, Contabilidad, Administración, Economía'),(173,'Adebol','Salud, Sector Público y Fundaciones','','Operación de servicios corporativos y programas institucionales de apoyo.','Administración, Psicología'),(174,'Farmacias Similares','Salud, Sector Público y Fundaciones','https://www.farmaciassimilares.com','Venta de medicamentos genéricos y servicios de consulta médica ambulatoria.','Mercadotecnia, Administración, Psicología, Nutrición'),(175,'Fundación Treviño Elizondo','Salud, Sector Público y Fundaciones','','Institución de beneficencia para el fomento educativo, becas y desarrollo comunitario.','Psicología, Educación, Administración'),(176,'ICAT','Salud, Sector Público y Fundaciones','','Capacitación para el trabajo y desarrollo de competencias laborales técnicas.','Psicología, Pedagogía, Administración'),(177,'IMSS','Salud, Sector Público y Fundaciones','https://www.imss.gob.mx','Seguridad social, atención médica hospitalaria y prestaciones para trabajadores en México.','Nutrición, Psicología, Administración, Derecho'),(178,'INEGI','Salud, Sector Público y Fundaciones','https://www.inegi.org.mx','Captura, análisis y difusión de información estadística y geográfica nacional.','Ing. en Software, Ing. en Sistemas, Finanzas, Administración'),(179,'Salud Digna','Salud, Sector Público y Fundaciones','https://www.saluddigna.com','Servicios de diagnóstico clínico de bajo costo: laboratorio, ultrasonido, mastografía y lentes.','Nutrición, Administración, Ing. en Sistemas, Psicología'),(180,'Secretaría de Salud','Salud, Sector Público y Fundaciones','https://www.gob.mx/salud','Regulación, políticas sanitarias y administración del sistema de salud pública.','Nutrición, Psicología, Derecho, Administración'),(181,'SEP','Salud, Sector Público y Fundaciones','https://www.gob.mx/sep','Diseño, ejecución y supervisión de las políticas educativas a nivel nacional.','Psicología, Pedagogía, Administración, Derecho'),(182,'Teleflex Medical','Salud, Sector Público y Fundaciones','https://www.teleflex.com','Fabricación de dispositivos médicos especializados para cirugía, urología y cuidados intensivos.','Ing. Industrial, Ing. Mecatrónica, Nutrición / Salud');
/*!40000 ALTER TABLE `empresas_vinculadas` ENABLE KEYS */;
UNLOCK TABLES;

-- Usuario demo (logueable) y su estudiante: al07080560@tecmilenio.mx / DemoMaps2026!
INSERT INTO usuarios (id_usuario, nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, foto_url, activo)
VALUES (12, 'Daniel Alejandro Silva Rosas', 'al07080560@tecmilenio.mx', '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy', 'estudiante', 50, NULL, 1);
INSERT INTO estudiantes (id_estudiante, id_usuario, id_carrera, matricula, semestre_actual, proposito_vida)
VALUES (6, 12, 3, 'AL07080560', 3, 'Convertirme en un ingeniero de software que transforme procesos con tecnologia.');

-- Usuario administrador (logueable): admin@tecmilenio.mx / DemoMaps2026!
INSERT INTO usuarios (id_usuario, nombre_completo, correo, contrasena_hash, rol, puntos_reputacion, foto_url, activo)
VALUES (14, 'Administrador MAPS', 'admin@tecmilenio.mx', '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy', 'admin', 100, NULL, 1);

-- =====================================================================
-- 3) DATOS DEMO
-- =====================================================================

-- ------------------------------------------------
-- Seed: seed-demo.sql
-- ------------------------------------------------
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

-- ------------------------------------------------
-- Seed: seed-follows.sql
-- ------------------------------------------------
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

-- ------------------------------------------------
-- Seed: seed-mensajes-tips.sql
-- ------------------------------------------------
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

-- Crear el profesor (idempotente): profesor.demo@tecmilenio.mx / DemoMaps2026!
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

-- Cuentas para verificar los flujos de docente y Semestre Empresarial.
-- Ambas usan la contraseña DemoMaps2026!
SET @test_password_hash = '$2b$12$6ELKRVnF19kwrvOn3x.o9.25zkiOUOlKS.zw1dlEr9rn7UyKjSaCy';

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

SET FOREIGN_KEY_CHECKS = 1;

