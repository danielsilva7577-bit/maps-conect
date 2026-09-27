CREATE TABLE IF NOT EXISTS refresh_tokens (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    token          VARCHAR(255) NOT NULL,
    id_usuario     INT          NOT NULL,
    fecha_expira   DATETIME     NOT NULL,
    revocado       TINYINT(1)   NOT NULL DEFAULT 0,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_token (token),
    KEY idx_ref_usuario (id_usuario),
    CONSTRAINT fk_ref_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
