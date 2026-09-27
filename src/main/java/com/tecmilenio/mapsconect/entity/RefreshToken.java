package com.tecmilenio.mapsconect.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Token opaco de refresco JWT. Almacenado en BD con expiración de 30 días.
 * Se invalida (revocado=true) al usar o al hacer logout.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "fecha_expira", nullable = false)
    private LocalDateTime fechaExpira;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revocado = false;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public boolean estaVigente() {
        return !Boolean.TRUE.equals(revocado)
                && LocalDateTime.now().isBefore(fechaExpira);
    }
}
