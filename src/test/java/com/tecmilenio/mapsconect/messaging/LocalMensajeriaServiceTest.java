package com.tecmilenio.mapsconect.messaging;

import com.tecmilenio.mapsconect.dto.MensajeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalMensajeriaServiceTest {

    @Mock
    private DespachadorEventosLocal despachador;

    private LocalMensajeriaService mensajeriaService;

    @BeforeEach
    void setUp() {
        mensajeriaService = new LocalMensajeriaService();
        mensajeriaService.setDespachador(despachador);
    }

    @Test
    void esDistribuido_devuelveFalseParaModoLocal() {
        assertThat(mensajeriaService.esDistribuido()).isFalse();
    }

    @Test
    void publicarMensajeChat_despachaLocalmente() {
        MensajeDTO dto = MensajeDTO.builder().id(1).texto("Hola").build();
        mensajeriaService.publicarMensajeChat(10, dto, 25);

        verify(despachador).despacharMensajeLocal(10, dto);
        verify(despachador).despacharNotificacionDestinatarioLocal(25, 10, dto);
    }

    @Test
    void publicarNotificacion_despachaLocalmente() {
        Map<String, Object> payload = Map.of("tipo", "foro", "titulo", "Nueva respuesta");
        mensajeriaService.publicarNotificacion(50, payload);

        verify(despachador).despacharNotificacionUsuarioLocal(50, payload);
    }

    @Test
    void publicarTyping_despachaLocalmente() {
        mensajeriaService.publicarTyping(10, "test@tecmilenio.mx", true);

        verify(despachador).despacharTypingLocal(10, "test@tecmilenio.mx", true);
    }
}
