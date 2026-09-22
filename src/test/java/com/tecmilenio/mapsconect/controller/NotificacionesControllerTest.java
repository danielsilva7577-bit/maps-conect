package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.security.RateLimitingProperties;
import com.tecmilenio.mapsconect.security.RateLimiterService;
import com.tecmilenio.mapsconect.service.MensajeService;
import com.tecmilenio.mapsconect.service.NotificacionService;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para NotificacionesController — cubre endpoints REST y SSE.
 *
 * Estrategia de autenticación:
 * <ul>
 *   <li>{@code @WebMvcTest} aísla la capa web.</li>
 *   <li>{@code addFilters = false} evita la cadena de filtros HTTP.</li>
 *   <li>{@code @WithMockUser} establece la Authentication en
 *       {@code SecurityContextHolder} antes de cada test.</li>
 *   <li>{@code .principal(auth)} en cada petición inyecta la Authentication
 *       como Principal en el MockHttpServletRequest.</li>
 *   <li>El ServletRequestMethodArgumentResolver nativo lee
 *       getUserPrincipal() y resuelve el parámetro Authentication.</li>
 * </ul>
 */
@WebMvcTest(NotificacionesController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser("alumno@tecmilenio.mx")
class NotificacionesControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private MensajeService mensajeService;
    @MockBean private NotificacionService notificacionService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private RateLimiterService rateLimiterService;
    @MockBean private RateLimitingProperties rateLimitingProperties;

    private final String EMAIL = "alumno@tecmilenio.mx";
    private Authentication auth;

    @BeforeEach
    void setUp() {
        when(rateLimitingProperties.limitFor(anyString()))
                .thenReturn(new RateLimitingProperties.Limit(60, 60));
        when(rateLimitingProperties.defaults())
                .thenReturn(new RateLimitingProperties.Limit(60, 60));

        auth = SecurityContextHolder.getContext().getAuthentication();
    }

    /** Helper: adds the Authentication as Principal to any request builder. */
    private MockHttpServletRequestBuilder withAuth(MockHttpServletRequestBuilder b) {
        if (auth != null) {
            b.principal(auth);
        }
        return b;
    }

    private Usuario usuario() {
        return Usuario.builder().id(1).email(EMAIL).nombreCompleto("Juan Pérez")
                .rol(Usuario.Rol.ESTUDIANTE).activo(true).build();
    }

    // ─── RESUMEN DE NOTIFICACIONES ────────────────────────────────────────────

    @Test
    void resumen_conAuth_devuelve200ConDatos() throws Exception {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario()));

        Map<String, Object> resumenMensajes = new LinkedHashMap<>();
        resumenMensajes.put("noLeidos", 3L);
        resumenMensajes.put("items", new ArrayList<>());

        Map<String, Object> resumenGenerales = new LinkedHashMap<>();
        resumenGenerales.put("noLeidos", 2L);
        Map<String, Object> itemGeneral = new LinkedHashMap<>();
        itemGeneral.put("id", 1);
        itemGeneral.put("tipo", "foro");
        itemGeneral.put("nombre", "Nueva respuesta en foro");
        resumenGenerales.put("items", List.of(itemGeneral));

        when(mensajeService.resumenNotificaciones(EMAIL)).thenReturn(resumenMensajes);
        when(notificacionService.resumenGenerales(EMAIL)).thenReturn(resumenGenerales);

        mockMvc.perform(withAuth(get("/notificaciones/resumen")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.noLeidos").value(5))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].tipo").value("foro"));

        verify(mensajeService).resumenNotificaciones(EMAIL);
        verify(notificacionService).resumenGenerales(EMAIL);
    }

    // ─── SSE STREAM ─────────────────────────────────────────────────────────────

    @Test
    void streamNotificaciones_conAuth_devuelve200ConContentTypeSse() throws Exception {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario()));

        SseEmitter emitter = new SseEmitter(5000L);
        when(notificacionService.notificacionesStream(EMAIL)).thenReturn(emitter);

        MvcResult mvcResult = mockMvc.perform(withAuth(get("/notificaciones/stream")))
                .andExpect(request().asyncStarted())
                .andReturn();

        emitter.complete();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));

        verify(notificacionService).notificacionesStream(EMAIL);
    }

    // ─── MARCAR TODAS COMO LEÍDAS ────────────────────────────────────────────

    @Test
    void marcarTodas_exitoso_devuelve200() throws Exception {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario()));

        mockMvc.perform(withAuth(post("/notificaciones/marcar-todas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Notificaciones marcadas como leídas"));

        verify(mensajeService).marcarTodasLeidas(EMAIL);
        verify(notificacionService).marcarTodasLeidas(1);
    }
}


