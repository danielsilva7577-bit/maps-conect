package com.tecmilenio.mapsconect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecmilenio.mapsconect.dto.ConversacionDTO;
import com.tecmilenio.mapsconect.dto.DetalleConversacionDTO;
import com.tecmilenio.mapsconect.dto.MensajeDTO;
import com.tecmilenio.mapsconect.dto.MensajeRequestDTO;
import com.tecmilenio.mapsconect.exception.ResourceNotFoundException;
import com.tecmilenio.mapsconect.security.RateLimitingProperties;
import com.tecmilenio.mapsconect.security.RateLimiterService;
import com.tecmilenio.mapsconect.service.MensajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para MensajesController — cubre endpoints REST y SSE.
 *
 * Estrategia de autenticación:
 * <ul>
 *   <li>{@code @WebMvcTest} aísla la capa web.</li>
 *   <li>{@code addFilters = false} evita la cadena de filtros HTTP.</li>
 *   <li>{@code @WithMockUser} establece la Authentication en
 *       {@code SecurityContextHolder} (ThreadLocal) antes de cada test.</li>
 *   <li>{@code .principal(auth)} en cada petición inyecta la
 *       Authentication como {@code Principal} en el {@code MockHttpServletRequest}.</li>
 *   <li>El {@code ServletRequestMethodArgumentResolver} nativo de Spring lee
 *       {@code request.getUserPrincipal()} y resuelve el parámetro
 *       {@code Authentication} directamente, sin resolvers personalizados.</li>
 * </ul>
 *
 * El endpoint SSE /mensajes/{id}/stream se prueba con asyncDispatch:
 * se crea un SseEmitter real, se completa manualmente para finalizar el
 * procesamiento async y verificar content-type + status 200.
 */
@WebMvcTest(MensajesController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser("alumno@tecmilenio.mx")
class MensajesControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MensajeService mensajeService;
    @MockBean private RateLimiterService rateLimiterService;
    @MockBean private RateLimitingProperties rateLimitingProperties;

    private final String EMAIL = "alumno@tecmilenio.mx";
    private Authentication auth;

    @BeforeEach
    void setUp() {
        // RateLimitingInterceptor needs limitFor() and defaults() to return non-null Limit
        when(rateLimitingProperties.limitFor(anyString()))
                .thenReturn(new RateLimitingProperties.Limit(60, 60));
        when(rateLimitingProperties.defaults())
                .thenReturn(new RateLimitingProperties.Limit(60, 60));

        // Authentication from @WithMockUser, used as Principal for all requests
        auth = SecurityContextHolder.getContext().getAuthentication();
    }

    /** Helper: adds the Authentication as Principal to any request builder. */
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder b) {
        if (auth != null) {
            b.principal(auth);
        }
        return b;
    }

    // ─── LISTAR CONVERSACIONES ─────────────────────────────────────────────────

    @Test
    void listarConversaciones_conAuth_devuelve200ConLista() throws Exception {
        ConversacionDTO dto = ConversacionDTO.builder()
                .id(1).idUsuario(2).nombre("Prof. García")
                .rol("PROFESOR").foto(null).esProf(true)
                .preview("Hola").tiempo("10:30").fechaUltimoMensaje("2024-01-01T10:30")
                .build();

        when(mensajeService.listarConversaciones(EMAIL)).thenReturn(List.of(dto));

        mockMvc.perform(withAuth(get("/mensajes")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].nombre").value("Prof. García"))
                .andExpect(jsonPath("$.data[0].esProf").value(true));

        verify(mensajeService).listarConversaciones(EMAIL);
    }

    // ─── CREAR CONVERSACIÓN ────────────────────────────────────────────────────

    @Test
    void crearConversacion_exitoso_devuelve201ConConversacion() throws Exception {
        DetalleConversacionDTO detalle = DetalleConversacionDTO.builder()
                .id(1).idUsuario(2).nombre("Prof. García")
                .subtitulo("PROFESOR").foto("https://img.com/foto.jpg").esProf(true)
                .build();

        when(mensajeService.crearConversacion(EMAIL, 2)).thenReturn(1);
        when(mensajeService.obtenerDetalle(EMAIL, 1)).thenReturn(detalle);

        mockMvc.perform(withAuth(post("/mensajes/nuevo/2")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.idUsuario").value(2))
                .andExpect(jsonPath("$.data.nombre").value("Prof. García"))
                .andExpect(jsonPath("$.data.esProf").value(true));

        verify(mensajeService).crearConversacion(EMAIL, 2);
        verify(mensajeService).obtenerDetalle(EMAIL, 1);
    }

    @Test
    void crearConversacion_usuarioInexistente_devuelve404() throws Exception {
        when(mensajeService.crearConversacion(EMAIL, 999))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        mockMvc.perform(withAuth(post("/mensajes/nuevo/999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(mensajeService).crearConversacion(EMAIL, 999);
    }

    // ─── ENVIAR MENSAJE ────────────────────────────────────────────────────────

    @Test
    void enviarMensaje_exitoso_devuelve201ConMensaje() throws Exception {
        MensajeDTO mensaje = MensajeDTO.builder()
                .id(10).texto("Hola profesor").tiempo("10:30")
                .fechaEnvio("2024-01-01T10:30").propio(true).leido(false)
                .autorNombre("Juan").autorId(1)
                .adjuntoNombre(null).adjuntoTipo(null).adjuntoTamano(null)
                .build();

        when(mensajeService.enviarMensaje(EMAIL, 1, "Hola profesor")).thenReturn(mensaje);

        MensajeRequestDTO body = new MensajeRequestDTO();
        body.setTexto("Hola profesor");

        mockMvc.perform(withAuth(post("/mensajes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.texto").value("Hola profesor"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.propio").value(true));

        verify(mensajeService).enviarMensaje(EMAIL, 1, "Hola profesor");
    }

    @Test
    void enviarMensaje_conTextoVacio_devuelve400() throws Exception {
        MensajeRequestDTO body = new MensajeRequestDTO();
        body.setTexto("");

        mockMvc.perform(withAuth(post("/mensajes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mensajeService);
    }

    @Test
    void enviarMensaje_conTextoNulo_devuelve400() throws Exception {
        MensajeRequestDTO body = new MensajeRequestDTO();
        body.setTexto(null);

        mockMvc.perform(withAuth(post("/mensajes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mensajeService);
    }

    // ─── SSE STREAM ────────────────────────────────────────────────────────────

    @Test
    void streamMensajes_conAuth_devuelve200ConContentTypeSse() throws Exception {
        SseEmitter emitter = new SseEmitter(5000L);
        when(mensajeService.suscribirse(eq(1), eq(EMAIL))).thenReturn(emitter);

        MvcResult mvcResult = mockMvc.perform(withAuth(get("/mensajes/1/stream")))
                .andExpect(request().asyncStarted())
                .andReturn();

        emitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(mensajeService).suscribirse(1, EMAIL);
    }

    @Test
    void streamMensajes_conConversacionInexistente_devuelve404() throws Exception {
        when(mensajeService.suscribirse(eq(999), eq(EMAIL)))
                .thenThrow(new ResourceNotFoundException("Conversación no encontrada"));

        mockMvc.perform(withAuth(get("/mensajes/999/stream")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(mensajeService).suscribirse(999, EMAIL);
    }

    // ─── MARCAR LEÍDOS ────────────────────────────────────────────────────────

    @Test
    void marcarLeidos_exitoso_devuelve200() throws Exception {
        mockMvc.perform(withAuth(post("/mensajes/1/leido")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Mensajes marcados como leídos"));

        verify(mensajeService).marcarLeidos(EMAIL, 1);
    }
}


