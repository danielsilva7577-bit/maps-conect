package com.tecmilenio.mapsconect.controller;

import com.tecmilenio.mapsconect.dto.ApiResponse;
import com.tecmilenio.mapsconect.dto.RegistroDTO;
import com.tecmilenio.mapsconect.entity.Usuario;
import com.tecmilenio.mapsconect.repository.EmpresaVinculadaRepository;
import com.tecmilenio.mapsconect.repository.EstudianteRepository;
import com.tecmilenio.mapsconect.repository.ProfesorRepository;
import com.tecmilenio.mapsconect.repository.PublicacionRepository;
import com.tecmilenio.mapsconect.repository.RecursoAcademicoRepository;
import com.tecmilenio.mapsconect.repository.ResenaEmpresarialRepository;
import com.tecmilenio.mapsconect.repository.SesionRepasoRepository;
import com.tecmilenio.mapsconect.repository.UsuarioRepository;
import com.tecmilenio.mapsconect.service.ReporteInstitucionalService;
import com.tecmilenio.mapsconect.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador administrativo (panel de administración).
 *
 * <p>Expone funcionalidades exclusivas para usuarios con rol
 * {@code ADMINISTRADOR}: crear docentes, listar e inhabilitar/reactivar
 * cuentas de estudiantes y profesores, consultar estadísticas globales,
 * y descargar reportes institucionales en formato CSV o PDF.</p>
 *
 * <p>Todos los endpoints verifican permisos de administrador mediante
 * el método {@link #esAdmin(Authentication)}.</p>
 *
 * @see com.tecmilenio.mapsconect.service.ReporteInstitucionalService
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM");

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EstudianteRepository estudianteRepository;
    @Autowired
    private ProfesorRepository profesorRepository;
    @Autowired
    private PublicacionRepository publicacionRepository;
    @Autowired
    private SesionRepasoRepository sesionRepasoRepository;
    @Autowired
    private EmpresaVinculadaRepository empresaRepository;
    @Autowired
    private ResenaEmpresarialRepository resenaRepository;
    @Autowired
    private RecursoAcademicoRepository recursoRepository;

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ReporteInstitucionalService reporteInstitucionalService;

    @PostMapping("/crear-docente")
    public ResponseEntity<ApiResponse<?>> crearDocente(
            @Valid @RequestBody RegistroDTO registroDTO,
            Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "No autenticado"));
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        if (usuario == null || usuario.getRol() != Usuario.Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        registroDTO.setRol("PROFESOR");
        try {
            usuarioService.registrar(registroDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (com.tecmilenio.mapsconect.exception.ConflictoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(HttpStatus.CONFLICT.value(), e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Docente creado correctamente"));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarUsuarios(
            @RequestParam(required = false) String rol,
            Authentication authentication) {
        if (!esAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()) {
            if (rol != null && !rol.isBlank()
                    && !u.getRol().name().equalsIgnoreCase(rol)) {
                continue;
            }
            if (u.getRol() == Usuario.Rol.ADMINISTRADOR) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getId());
            item.put("nombre", u.getNombreCompleto());
            item.put("email", u.getEmail());
            item.put("rol", u.getRol().name());
            item.put("activo", u.getActivo());
            item.put("fechaRegistro", u.getFechaRegistro());
            if (u.getRol() == Usuario.Rol.ESTUDIANTE) {
                estudianteRepository.findByIdUsuario(u.getId())
                        .ifPresent(est -> item.put("matricula", est.getMatricula()));
            } else if (u.getRol() == Usuario.Rol.PROFESOR) {
                profesorRepository.findByIdUsuario(u.getId())
                        .ifPresent(prof -> item.put("nomina", prof.getNumeroNomina()));
            }
            lista.add(item);
        }
        lista.sort((a, b) -> Boolean.compare(!(Boolean) b.get("activo"), !(Boolean) a.get("activo")));
        return ResponseEntity.ok(ApiResponse.success(lista, "Usuarios obtenidos"));
    }

    @PostMapping("/usuarios/{id}/inhabilitar")
    public ResponseEntity<ApiResponse<?>> inhabilitarUsuario(
            @PathVariable Integer id, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado"));
        }
        if (usuario.getRol() == Usuario.Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "No se puede inhabilitar a un administrador"));
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.success(null, "Cuenta inhabilitada"));
    }

    @PostMapping("/usuarios/{id}/reactivar")
    public ResponseEntity<ApiResponse<?>> reactivarUsuario(
            @PathVariable Integer id, Authentication authentication) {
        if (!esAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "Usuario no encontrado"));
        }

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.success(null, "Cuenta reactivada"));
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return false;
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        return usuario != null && usuario.getRol() == Usuario.Rol.ADMINISTRADOR;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> stats(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "No autenticado"));
        }

        Usuario usuario = usuarioRepository.findByEmail(authentication.getName()).orElse(null);
        if (usuario == null || usuario.getRol() != Usuario.Rol.ADMINISTRADOR) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("perfil", perfil(usuario));
        data.put("kpis", kpis());
        data.put("actividad", actividadReciente());

        return ResponseEntity.ok(ApiResponse.success(data, "Estadísticas del panel obtenidas"));
    }

    @GetMapping("/reportes/datos")
    public ResponseEntity<ApiResponse<ReporteInstitucionalService.Reporte>> datosReporte(
            @RequestParam(defaultValue = "circulos") String tipo,
            @RequestParam(defaultValue = "current") String ciclo,
            Authentication authentication) {
        if (!esAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }
        try {
            ReporteInstitucionalService.Reporte reporte = reporteInstitucionalService.generar(tipo, ciclo);
            return ResponseEntity.ok(ApiResponse.success(reporte, "Reporte institucional obtenido"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    @GetMapping("/reportes")
    public ResponseEntity<?> descargarReporte(
            @RequestParam(defaultValue = "circulos") String tipo,
            @RequestParam(defaultValue = "current") String ciclo,
            @RequestParam(defaultValue = "csv") String formato,
            Authentication authentication) {
        if (!esAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Se requiere rol de administrador"));
        }

        String formatoNormalizado = formato == null ? "" : formato.trim().toLowerCase();
        if (!"csv".equals(formatoNormalizado) && !"pdf".equals(formatoNormalizado)
                && !"json".equals(formatoNormalizado) && !"excel".equals(formatoNormalizado)
                && !"xls".equals(formatoNormalizado)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Formato de reporte no válido. Usa pdf, excel, csv o json"));
        }

        try {
            ReporteInstitucionalService.Reporte reporte = reporteInstitucionalService.generar(tipo, ciclo);
            if ("json".equals(formatoNormalizado)) {
                return ResponseEntity.ok(ApiResponse.success(reporte, "Reporte institucional obtenido"));
            }

            boolean esPdf = "pdf".equals(formatoNormalizado);
            boolean esExcel = "excel".equals(formatoNormalizado) || "xls".equals(formatoNormalizado);
            byte[] archivo;
            String extension;
            MediaType mediaType;

            if (esPdf) {
                archivo = reporteInstitucionalService.comoPdf(reporte);
                extension = "pdf";
                mediaType = MediaType.APPLICATION_PDF;
            } else if (esExcel) {
                archivo = reporteInstitucionalService.comoExcel(reporte);
                extension = "xls";
                mediaType = MediaType.parseMediaType("application/vnd.ms-excel;charset=UTF-8");
            } else {
                archivo = reporteInstitucionalService.comoCsv(reporte);
                extension = "csv";
                mediaType = MediaType.parseMediaType("text/csv;charset=UTF-8");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(archivo.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + reporteInstitucionalService.nombreArchivo(reporte, extension) + "\"");
            return new ResponseEntity<>(archivo, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        }
    }

    private Map<String, Object> perfil(Usuario usuario) {
        Map<String, Object> perfil = new LinkedHashMap<>();
        perfil.put("nombre", usuario.getNombreCompleto());
        perfil.put("email", usuario.getEmail());
        perfil.put("rol", usuario.getRol().name());
        perfil.put("foto", usuario.getFotoUrl());
        return perfil;
    }

    private Map<String, Object> kpis() {
        long totalEstudiantes = estudianteRepository.count();
        long totalProfesores = profesorRepository.count();
        long totalPublicaciones = publicacionRepository.count();
        long sesionesAbiertas = sesionRepasoRepository.count();
        long empresas = empresaRepository.count();
        long resenas = resenaRepository.count();
        long recursos = recursoRepository.count();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalEstudiantes", totalEstudiantes);
        kpis.put("totalProfesores", totalProfesores);
        kpis.put("totalPublicaciones", totalPublicaciones);
        kpis.put("sesionesAbiertas", sesionesAbiertas);
        kpis.put("totalEmpresas", empresas);
        kpis.put("totalResenas", resenas);
        kpis.put("totalRecursos", recursos);
        return kpis;
    }

    private List<Map<String, Object>> actividadReciente() {
        List<Map<String, Object>> lista = new ArrayList<>();

        for (Object[] fila : publicacionRepository.findForoConDetalles(5)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("modulo", "Foro Dudas");
            item.put("titulo", fila[1]);
            item.put("autor", fila[6]);
            item.put("tipo", "publicacion");
            item.put("estado", fila[3]);
            Object fecha = fila[4];
            item.put("fecha", fecha instanceof LocalDateTime lt ? lt : null);
            lista.add(item);
        }
        return lista;
    }
}
