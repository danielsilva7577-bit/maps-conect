package com.tecmilenio.mapsconect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO con el detalle completo de una conversacion (datos del otro usuario y mensajes paginados).
 *
 * <p>Los mensajes se devuelven paginados (más recientes primero dentro del rango solicitado).
 * El frontend puede implementar scroll infinito hacia atrás pidiendo páginas mayores.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleConversacionDTO {

    private Integer id;
    private Integer idUsuario;
    private String nombre;
    private String subtitulo;
    private String foto;
    private boolean esProf;
    private boolean enLinea;

    @Builder.Default
    private List<MensajeDTO> mensajes = new ArrayList<>();

    /** Página actual de mensajes (0-indexed). */
    @Builder.Default
    private int pagina = 0;

    /** true si existen mensajes anteriores (páginas mayores disponibles). */
    @Builder.Default
    private boolean hayMas = false;

    /** Total de mensajes en la conversación. */
    @Builder.Default
    private long totalMensajes = 0;

}
