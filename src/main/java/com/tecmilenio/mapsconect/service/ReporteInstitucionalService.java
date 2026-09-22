package com.tecmilenio.mapsconect.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Construye los reportes institucionales a partir de los datos reales de la
 * plataforma y los convierte a CSV o PDF sin depender del navegador.
 */
@Service
public class ReporteInstitucionalService {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JdbcTemplate jdbcTemplate;

    public ReporteInstitucionalService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Reporte generar(String tipoSolicitado, String cicloSolicitado) {
        TipoReporte tipo = TipoReporte.desde(tipoSolicitado);
        Periodo periodo = Periodo.desde(cicloSolicitado);

        return switch (tipo) {
            case CIRCULOS -> reporteCirculos(periodo);
            case DUDAS -> reporteDudas(periodo);
            case RECURSOS -> reporteRecursos(periodo);
            case EMPRESAS -> reporteEmpresas(periodo);
        };
    }

    public String nombreArchivo(Reporte reporte, String extension) {
        return "reporte-" + reporte.codigo() + "-" + reporte.cicloArchivo() + "." + extension;
    }

    public byte[] comoCsv(Reporte reporte) {
        List<List<String>> filas = new ArrayList<>();
        filas.add(List.of("MAPS Connect - Reporte institucional", ""));
        filas.add(List.of("Reporte", reporte.titulo()));
        filas.add(List.of("Periodo", reporte.periodo()));
        filas.add(List.of("Generado", FECHA_HORA.format(reporte.generadoEn())));
        filas.add(padRow(List.of(), 2));
        filas.add(List.of("INDICADOR", "VALOR"));
        for (Indicador indicador : reporte.indicadores()) {
            filas.add(List.of(indicador.nombre(), indicador.valor()));
        }

        for (String nota : reporte.notas()) {
            filas.add(padRow(List.of(), 2));
            filas.add(List.of("NOTA", nota));
        }

        for (Seccion seccion : reporte.secciones()) {
            int columnas = seccion.columnas().size();
            filas.add(padRow(List.of(), columnas));
            filas.add(padRow(List.of(seccion.titulo()), columnas));
            filas.add(seccion.columnas());
            if (seccion.filas().isEmpty()) {
                filas.add(padRow(List.of("Sin registros para el periodo seleccionado."), columnas));
            } else {
                for (List<String> fila : seccion.filas()) {
                    filas.add(padRow(fila, columnas));
                }
            }
        }

        String csv = filas.stream()
                .map(fila -> fila.stream().map(this::campoCsv).collect(Collectors.joining(";")))
                .collect(Collectors.joining("\r\n"));
        return ("\ufeffsep=;\r\n" + csv).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] comoExcel(Reporte reporte) {
        StringBuilder xml = new StringBuilder(64 * 1024);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>\r\n");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\r\n");
        xml.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\r\n");
        xml.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\r\n");
        xml.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\r\n");
        xml.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\r\n");

        xml.append(" <Styles>\r\n");
        xml.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\r\n");
        xml.append("   <Alignment ss:Vertical=\"Center\"/>\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#1F2937\"/>\r\n");
        xml.append("  </Style>\r\n");

        // Banner principal
        xml.append("  <Style ss:ID=\"HeaderPrincipal\">\r\n");
        xml.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"13\" ss:Color=\"#FFFFFF\" ss:Bold=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#005A2B\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("  </Style>\r\n");

        xml.append("  <Style ss:ID=\"MetaInfo\">\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#64748B\" ss:Italic=\"1\"/>\r\n");
        xml.append("  </Style>\r\n");

        // Seccion
        xml.append("  <Style ss:ID=\"TituloSeccion\">\r\n");
        xml.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"11\" ss:Color=\"#005A2B\" ss:Bold=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#F0FDF4\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#86EFAC\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");

        // Cabecera de tablas
        xml.append("  <Style ss:ID=\"Th\">\r\n");
        xml.append("   <Alignment ss:Horizontal=\"Left\" ss:Vertical=\"Center\"/>\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10\" ss:Color=\"#FFFFFF\" ss:Bold=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#004D25\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#003318\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");

        // Celdas de tablas (par / impar)
        xml.append("  <Style ss:ID=\"TdPar\">\r\n");
        xml.append("   <Interior ss:Color=\"#F8FAFC\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");
        xml.append("  <Style ss:ID=\"TdImpar\">\r\n");
        xml.append("   <Interior ss:Color=\"#FFFFFF\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#E2E8F0\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");

        // KPIs
        xml.append("  <Style ss:ID=\"KpiNombre\">\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9.5\" ss:Color=\"#334155\" ss:Bold=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#F1F5F9\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");
        xml.append("  <Style ss:ID=\"KpiValor\">\r\n");
        xml.append("   <Alignment ss:Horizontal=\"Right\" ss:Vertical=\"Center\"/>\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"10.5\" ss:Color=\"#005A2B\" ss:Bold=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#FFFFFF\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#CBD5E1\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");

        // Notas
        xml.append("  <Style ss:ID=\"Nota\">\r\n");
        xml.append("   <Font ss:FontName=\"Segoe UI\" ss:Size=\"9\" ss:Color=\"#92400E\" ss:Italic=\"1\"/>\r\n");
        xml.append("   <Interior ss:Color=\"#FFFBEB\" ss:Pattern=\"Solid\"/>\r\n");
        xml.append("   <Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#FDE68A\"/></Borders>\r\n");
        xml.append("  </Style>\r\n");

        xml.append(" </Styles>\r\n");

        // HOJA 1: RESUMEN EJECUTIVO
        xml.append(" <Worksheet ss:Name=\"Resumen\">\r\n");
        xml.append("  <Table ss:DefaultColumnWidth=\"180\">\r\n");
        xml.append("   <Column ss:Width=\"250\"/>\r\n");
        xml.append("   <Column ss:Width=\"140\"/>\r\n");

        // Fila 1 Banner
        xml.append("   <Row ss:Height=\"28\">\r\n");
        xml.append("    <Cell ss:MergeAcross=\"1\" ss:StyleID=\"HeaderPrincipal\"><Data ss:Type=\"String\">MAPS CONNECT - ").append(escaparXml(reporte.titulo())).append("</Data></Cell>\r\n");
        xml.append("   </Row>\r\n");

        // Fila 2 Metadatos
        xml.append("   <Row ss:Height=\"18\">\r\n");
        xml.append("    <Cell ss:MergeAcross=\"1\" ss:StyleID=\"MetaInfo\"><Data ss:Type=\"String\">Ciclo: ")
                .append(escaparXml(reporte.periodo())).append(" | Generado: ").append(FECHA_HORA.format(reporte.generadoEn())).append("</Data></Cell>\r\n");
        xml.append("   </Row>\r\n");
        xml.append("   <Row ss:Height=\"10\"></Row>\r\n");

        // Fila 4 Titulo Indicadores
        xml.append("   <Row ss:Height=\"22\">\r\n");
        xml.append("    <Cell ss:MergeAcross=\"1\" ss:StyleID=\"TituloSeccion\"><Data ss:Type=\"String\">INDICADORES CLAVE DEL PERIODO (KPIs)</Data></Cell>\r\n");
        xml.append("   </Row>\r\n");

        // Filas de Indicadores
        for (Indicador ind : reporte.indicadores()) {
            xml.append("   <Row ss:Height=\"19\">\r\n");
            xml.append("    <Cell ss:StyleID=\"KpiNombre\"><Data ss:Type=\"String\">").append(escaparXml(ind.nombre())).append("</Data></Cell>\r\n");
            xml.append("    <Cell ss:StyleID=\"KpiValor\">").append(celdaDatoXml(ind.valor())).append("</Cell>\r\n");
            xml.append("   </Row>\r\n");
        }

        // Notas
        if (reporte.notas() != null && !reporte.notas().isEmpty()) {
            xml.append("   <Row ss:Height=\"10\"></Row>\r\n");
            for (String nota : reporte.notas()) {
                xml.append("   <Row ss:Height=\"20\">\r\n");
                xml.append("    <Cell ss:MergeAcross=\"1\" ss:StyleID=\"Nota\"><Data ss:Type=\"String\">Nota: ").append(escaparXml(nota)).append("</Data></Cell>\r\n");
                xml.append("   </Row>\r\n");
            }
        }

        xml.append("  </Table>\r\n");
        xml.append(" </Worksheet>\r\n");

        // HOJAS PARA CADA SECCIÓN DE DATOS
        int secIdx = 1;
        for (Seccion sec : reporte.secciones()) {
            String sheetName = limpiarNombreHoja(sec.titulo(), secIdx++);
            xml.append(" <Worksheet ss:Name=\"").append(escaparXml(sheetName)).append("\">\r\n");
            xml.append("  <Table ss:DefaultColumnWidth=\"120\">\r\n");

            int colCount = sec.columnas().size();
            for (int c = 0; c < colCount; c++) {
                int w = Math.min(Math.max(sec.columnas().get(c).length() * 11, 100), 280);
                xml.append("   <Column ss:Width=\"").append(w).append("\"/>\r\n");
            }

            // Titulo de seccion
            xml.append("   <Row ss:Height=\"24\">\r\n");
            xml.append("    <Cell ss:MergeAcross=\"").append(Math.max(colCount - 1, 0)).append("\" ss:StyleID=\"TituloSeccion\"><Data ss:Type=\"String\">")
                    .append(escaparXml(sec.titulo())).append("</Data></Cell>\r\n");
            xml.append("   </Row>\r\n");

            // Cabeceras
            xml.append("   <Row ss:Height=\"22\">\r\n");
            for (String col : sec.columnas()) {
                xml.append("    <Cell ss:StyleID=\"Th\"><Data ss:Type=\"String\">").append(escaparXml(col)).append("</Data></Cell>\r\n");
            }
            xml.append("   </Row>\r\n");

            // Filas
            if (sec.filas().isEmpty()) {
                xml.append("   <Row ss:Height=\"20\">\r\n");
                xml.append("    <Cell ss:MergeAcross=\"").append(Math.max(colCount - 1, 0)).append("\" ss:StyleID=\"TdPar\"><Data ss:Type=\"String\">Sin registros para el periodo seleccionado.</Data></Cell>\r\n");
                xml.append("   </Row>\r\n");
            } else {
                boolean par = false;
                for (List<String> fila : sec.filas()) {
                    String style = par ? "TdPar" : "TdImpar";
                    par = !par;
                    xml.append("   <Row ss:Height=\"18\">\r\n");
                    for (int c = 0; c < colCount; c++) {
                        String celda = c < fila.size() ? fila.get(c) : "";
                        xml.append("    <Cell ss:StyleID=\"").append(style).append("\">").append(celdaDatoXml(celda)).append("</Cell>\r\n");
                    }
                    xml.append("   </Row>\r\n");
                }
            }

            xml.append("  </Table>\r\n");
            xml.append(" </Worksheet>\r\n");
        }

        xml.append("</Workbook>\r\n");
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] comoPdf(Reporte reporte) {
        List<LineaPdf> lineas = new ArrayList<>();

        // 1. Tarjetas KPI tipo Dashboard
        if (reporte.indicadores() != null && !reporte.indicadores().isEmpty()) {
            lineas.add(LineaPdf.tarjetasKpi(reporte.indicadores()));
            lineas.add(LineaPdf.espacio(8));
        }

        // 2. Notas explicativas
        if (reporte.notas() != null) {
            for (String nota : reporte.notas()) {
                lineas.add(LineaPdf.nota(nota));
            }
            if (!reporte.notas().isEmpty()) {
                lineas.add(LineaPdf.espacio(6));
            }
        }

        // 3. Secciones y Tablas
        for (Seccion seccion : reporte.secciones()) {
            lineas.add(LineaPdf.seccionTitulo(seccion.titulo()));
            if (seccion.filas().isEmpty()) {
                lineas.add(LineaPdf.texto("Sin registros para el periodo seleccionado.", false, 8));
                lineas.add(LineaPdf.espacio(8));
                continue;
            }

            List<Float> anchos = calcularAnchosProporcionales(seccion.columnas(), seccion.filas());
            List<Float> xOffsets = calcularXOffsets(anchos);

            lineas.add(LineaPdf.tablaEncabezado(seccion.columnas(), xOffsets, anchos));
            boolean esPar = false;
            for (List<String> fila : seccion.filas()) {
                lineas.add(LineaPdf.tablaFila(padRow(fila, seccion.columnas().size()), xOffsets, anchos, esPar));
                esPar = !esPar;
            }
            lineas.add(LineaPdf.espacio(12));
        }

        return construirPdf(lineas, reporte);
    }

    private Reporte reporteCirculos(Periodo periodo) {
        String sql = """
                SELECT s.id_sesion AS id, s.titulo, s.materia, s.fecha, s.hora_inicio AS hora,
                       s.modalidad, s.ubicacion, s.estado, s.cupo_max AS cupo,
                       u.nombre_completo AS organizador, COUNT(i.id_usuario) AS inscritos
                FROM sesiones_repaso s
                JOIN usuarios u ON u.id_usuario = s.organizador_id
                LEFT JOIN sesion_inscripciones i ON i.id_sesion = s.id_sesion
                """ + filtroFecha("s.fecha", periodo) + """
                GROUP BY s.id_sesion, s.titulo, s.materia, s.fecha, s.hora_inicio, s.modalidad,
                         s.ubicacion, s.estado, s.cupo_max, u.nombre_completo
                ORDER BY s.fecha ASC, s.hora_inicio ASC, s.titulo ASC
                """;
        List<Map<String, Object>> datos = consultar(sql, periodo);

        long inscritos = datos.stream().mapToLong(fila -> numero(fila, "inscritos")).sum();
        long cupo = datos.stream().mapToLong(fila -> numero(fila, "cupo")).sum();
        long abiertas = datos.stream().filter(fila -> "ABIERTA".equalsIgnoreCase(texto(fila, "estado"))).count();
        long cerradas = datos.stream().filter(fila -> "CERRADA".equalsIgnoreCase(texto(fila, "estado"))).count();

        List<Indicador> indicadores = List.of(
                indicador("Sesiones programadas", datos.size()),
                indicador("Inscripciones registradas", inscritos),
                indicador("Cupo total programado", cupo),
                new Indicador("Ocupación de cupo", porcentaje(inscritos, cupo)),
                indicador("Sesiones abiertas", abiertas),
                indicador("Sesiones cerradas", cerradas));

        List<List<String>> filas = new ArrayList<>();
        for (Map<String, Object> fila : datos) {
            filas.add(List.of(
                    fecha(fila, "fecha"),
                    hora(fila, "hora"),
                    texto(fila, "titulo"),
                    texto(fila, "materia"),
                    texto(fila, "organizador"),
                    texto(fila, "modalidad"),
                    String.valueOf(numero(fila, "inscritos")),
                    String.valueOf(numero(fila, "cupo")),
                    texto(fila, "estado"),
                    textoConVacio(fila, "ubicacion", "Sin ubicación")));
        }

        Seccion seccion = new Seccion(
                "Detalle de círculos de estudio",
                List.of("Fecha", "Hora", "Círculo", "Materia", "Organizador", "Modalidad", "Inscritos", "Cupo", "Estado", "Ubicación"),
                filas);
        return nuevoReporte(TipoReporte.CIRCULOS, periodo, indicadores, List.of(seccion),
                List.of("La plataforma registra inscripciones. No existe un registro de asistencia física o virtual por sesión."));
    }

    private Reporte reporteDudas(Periodo periodo) {
        String sql = """
                SELECT p.id_publicacion AS id, p.titulo, p.fecha_publicacion AS fecha, p.estado,
                       COALESCE(m.nombre_materia, 'General') AS materia,
                       u.nombre_completo AS estudiante, COUNT(r.id_respuesta) AS respuestas,
                       CASE WHEN p.id_respuesta_aceptada IS NULL THEN 0 ELSE 1 END AS solucion_aceptada
                FROM publicaciones p
                JOIN estudiantes e ON e.id_estudiante = p.id_estudiante
                JOIN usuarios u ON u.id_usuario = e.id_usuario
                LEFT JOIN materias m ON m.id_materia = p.id_materia
                LEFT JOIN respuestas r ON r.id_publicacion = p.id_publicacion
                """ + filtroFecha("p.fecha_publicacion", periodo) + """
                GROUP BY p.id_publicacion, p.titulo, p.fecha_publicacion, p.estado, m.nombre_materia,
                         u.nombre_completo, p.id_respuesta_aceptada
                ORDER BY p.fecha_publicacion DESC, p.id_publicacion DESC
                """;
        List<Map<String, Object>> datos = consultar(sql, periodo);

        long resueltas = datos.stream().filter(fila -> "resuelta".equalsIgnoreCase(texto(fila, "estado"))).count();
        long abiertas = datos.stream().filter(fila -> "abierta".equalsIgnoreCase(texto(fila, "estado"))).count();
        long moderadas = datos.stream().filter(fila -> "moderada".equalsIgnoreCase(texto(fila, "estado"))).count();
        long respuestas = datos.stream().mapToLong(fila -> numero(fila, "respuestas")).sum();

        Map<String, ResumenMateria> resumen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map<String, Object> fila : datos) {
            String materia = texto(fila, "materia");
            ResumenMateria item = resumen.computeIfAbsent(materia, ignore -> new ResumenMateria());
            item.publicaciones++;
            item.respuestas += numero(fila, "respuestas");
            if ("resuelta".equalsIgnoreCase(texto(fila, "estado"))) {
                item.resueltas++;
            }
        }

        List<Indicador> indicadores = List.of(
                indicador("Dudas publicadas", datos.size()),
                indicador("Dudas resueltas", resueltas),
                indicador("Dudas abiertas", abiertas),
                indicador("Dudas moderadas", moderadas),
                indicador("Respuestas registradas", respuestas),
                new Indicador("Tasa de resolución", porcentaje(resueltas, datos.size())),
                indicador("Materias con actividad", resumen.size()));

        List<List<String>> resumenFilas = new ArrayList<>();
        for (Map.Entry<String, ResumenMateria> entrada : resumen.entrySet()) {
            ResumenMateria item = entrada.getValue();
            resumenFilas.add(List.of(
                    entrada.getKey(),
                    String.valueOf(item.publicaciones),
                    String.valueOf(item.resueltas),
                    String.valueOf(item.respuestas),
                    porcentaje(item.resueltas, item.publicaciones)));
        }

        List<List<String>> detalleFilas = new ArrayList<>();
        for (Map<String, Object> fila : datos) {
            detalleFilas.add(List.of(
                    fechaHora(fila, "fecha"),
                    texto(fila, "materia"),
                    texto(fila, "titulo"),
                    texto(fila, "estudiante"),
                    capitalizar(texto(fila, "estado")),
                    String.valueOf(numero(fila, "respuestas")),
                    siNo(numero(fila, "solucion_aceptada") > 0)));
        }

        return nuevoReporte(TipoReporte.DUDAS, periodo, indicadores, List.of(
                new Seccion("Resumen de resolución por materia", List.of("Materia", "Dudas", "Resueltas", "Respuestas", "Resolución"), resumenFilas),
                new Seccion("Detalle de dudas publicadas", List.of("Fecha", "Materia", "Duda", "Estudiante", "Estado", "Respuestas", "Solución aceptada"), detalleFilas)),
                List.of());
    }

    private Reporte reporteRecursos(Periodo periodo) {
        String sql = """
                SELECT r.id_recurso AS id, r.titulo, r.fecha_subida AS fecha, r.tipo_archivo AS tipo,
                       r.contador_descargas AS descargas, r.contador_reportes AS reportes,
                       r.oculto, COALESCE(m.nombre_materia, 'Sin materia') AS materia,
                       u.nombre_completo AS autor, COALESCE(r.adjunto_nombre, r.url_archivo) AS archivo
                FROM recursos_academicos r
                JOIN usuarios u ON u.id_usuario = r.id_usuario
                LEFT JOIN materias m ON m.id_materia = r.id_materia
                """ + filtroFecha("r.fecha_subida", periodo) + """
                ORDER BY r.fecha_subida DESC, r.id_recurso DESC
                """;
        List<Map<String, Object>> datos = consultar(sql, periodo);

        long descargas = datos.stream().mapToLong(fila -> numero(fila, "descargas")).sum();
        long reportes = datos.stream().mapToLong(fila -> numero(fila, "reportes")).sum();
        long ocultos = datos.stream().filter(fila -> booleano(fila, "oculto")).count();

        Map<String, ResumenRecurso> resumen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Map<String, Object> fila : datos) {
            ResumenRecurso item = resumen.computeIfAbsent(texto(fila, "materia"), ignore -> new ResumenRecurso());
            item.recursos++;
            item.descargas += numero(fila, "descargas");
            item.reportes += numero(fila, "reportes");
            if (booleano(fila, "oculto")) {
                item.ocultos++;
            }
        }

        List<Indicador> indicadores = List.of(
                indicador("Recursos publicados", datos.size()),
                indicador("Descargas registradas", descargas),
                new Indicador("Promedio de descargas por recurso", promedio(descargas, datos.size())),
                indicador("Reportes de moderación", reportes),
                indicador("Recursos ocultos", ocultos),
                indicador("Recursos visibles", datos.size() - ocultos),
                indicador("Materias con recursos", resumen.size()));

        List<List<String>> resumenFilas = new ArrayList<>();
        for (Map.Entry<String, ResumenRecurso> entrada : resumen.entrySet()) {
            ResumenRecurso item = entrada.getValue();
            resumenFilas.add(List.of(entrada.getKey(), String.valueOf(item.recursos),
                    String.valueOf(item.descargas), String.valueOf(item.reportes), String.valueOf(item.ocultos)));
        }

        List<List<String>> detalleFilas = new ArrayList<>();
        for (Map<String, Object> fila : datos) {
            detalleFilas.add(List.of(
                    fechaHora(fila, "fecha"),
                    texto(fila, "materia"),
                    texto(fila, "titulo"),
                    texto(fila, "autor"),
                    textoConVacio(fila, "tipo", "Sin tipo"),
                    String.valueOf(numero(fila, "descargas")),
                    String.valueOf(numero(fila, "reportes")),
                    booleano(fila, "oculto") ? "Oculto" : "Visible",
                    textoConVacio(fila, "archivo", "Sin archivo")));
        }

        return nuevoReporte(TipoReporte.RECURSOS, periodo, indicadores, List.of(
                new Seccion("Uso y moderación por materia", List.of("Materia", "Recursos", "Descargas", "Reportes", "Ocultos"), resumenFilas),
                new Seccion("Detalle de recursos académicos", List.of("Fecha", "Materia", "Recurso", "Autor", "Tipo", "Descargas", "Reportes", "Estado", "Archivo"), detalleFilas)),
                List.of("El reporte muestra métricas de uso y moderación. La plataforma no almacena una calificación de calidad del recurso."));
    }

    private Reporte reporteEmpresas(Periodo periodo) {
        String condicionPeriodo = periodo.todos()
                ? ""
                : " AND r.fecha_resena >= ? AND r.fecha_resena < ?";
        String resumenSql = """
                SELECT e.id_empresa AS id, e.nombre_empresa AS empresa,
                       COALESCE(e.sector, 'Sin sector') AS sector,
                       COUNT(r.id_resena) AS resenas,
                       COALESCE(AVG(r.calificacion), 0) AS promedio
                FROM empresas_vinculadas e
                LEFT JOIN resenas_empresarial r ON r.id_empresa = e.id_empresa
                """ + condicionPeriodo + """
                GROUP BY e.id_empresa, e.nombre_empresa, e.sector
                ORDER BY e.nombre_empresa ASC
                """;
        List<Map<String, Object>> resumenDatos = consultar(resumenSql, periodo);

        String detalleSql = """
                SELECT r.fecha_resena AS fecha, e.nombre_empresa AS empresa,
                       COALESCE(e.sector, 'Sin sector') AS sector, r.calificacion,
                       u.nombre_completo AS estudiante, es.semestre_actual AS semestre,
                       COALESCE(r.proyecto_desarrollado, 'Sin detalle') AS proyecto
                FROM resenas_empresarial r
                JOIN empresas_vinculadas e ON e.id_empresa = r.id_empresa
                JOIN estudiantes es ON es.id_estudiante = r.id_estudiante
                JOIN usuarios u ON u.id_usuario = es.id_usuario
                """ + filtroFecha("r.fecha_resena", periodo) + """
                ORDER BY r.fecha_resena DESC, r.id_resena DESC
                """;
        List<Map<String, Object>> detalleDatos = consultar(detalleSql, periodo);

        long empresasConResenas = resumenDatos.stream().filter(fila -> numero(fila, "resenas") > 0).count();
        long sumaCalificaciones = detalleDatos.stream().mapToLong(fila -> numero(fila, "calificacion")).sum();

        List<Indicador> indicadores = List.of(
                indicador("Empresas vinculadas", resumenDatos.size()),
                indicador("Empresas evaluadas en el periodo", empresasConResenas),
                indicador("Reseñas registradas", detalleDatos.size()),
                new Indicador("Calificación promedio", promedio(sumaCalificaciones, detalleDatos.size()) + " / 5"),
                indicador("Empresas sin reseñas en el periodo", resumenDatos.size() - empresasConResenas));

        List<List<String>> resumenFilas = new ArrayList<>();
        for (Map<String, Object> fila : resumenDatos) {
            resumenFilas.add(List.of(
                    texto(fila, "empresa"),
                    texto(fila, "sector"),
                    String.valueOf(numero(fila, "resenas")),
                    decimal(fila, "promedio") + " / 5"));
        }

        List<List<String>> detalleFilas = new ArrayList<>();
        for (Map<String, Object> fila : detalleDatos) {
            detalleFilas.add(List.of(
                    fechaHora(fila, "fecha"),
                    texto(fila, "empresa"),
                    texto(fila, "sector"),
                    texto(fila, "estudiante"),
                    String.valueOf(numero(fila, "semestre")),
                    String.valueOf(numero(fila, "calificacion")) + " / 5",
                    texto(fila, "proyecto")));
        }

        return nuevoReporte(TipoReporte.EMPRESAS, periodo, indicadores, List.of(
                new Seccion("Resumen por empresa", List.of("Empresa", "Sector", "Reseñas", "Promedio"), resumenFilas),
                new Seccion("Detalle de reseñas del periodo", List.of("Fecha", "Empresa", "Sector", "Estudiante", "Semestre", "Calificación", "Proyecto desarrollado"), detalleFilas)),
                List.of("El directorio completo se incluye en el resumen; las reseñas y calificaciones se limitan al periodo seleccionado."));
    }

    private Reporte nuevoReporte(TipoReporte tipo, Periodo periodo, List<Indicador> indicadores,
                                 List<Seccion> secciones, List<String> notas) {
        return new Reporte(tipo.codigo, tipo.titulo, periodo.etiqueta, periodo.codigoArchivo,
                LocalDateTime.now(), indicadores, secciones, notas);
    }

    private List<Map<String, Object>> consultar(String sql, Periodo periodo) {
        if (periodo.todos()) {
            return jdbcTemplate.queryForList(sql);
        }
        return jdbcTemplate.queryForList(sql,
                java.sql.Date.valueOf(periodo.inicio),
                java.sql.Date.valueOf(periodo.finExclusivo));
    }

    private String filtroFecha(String columna, Periodo periodo) {
        return periodo.todos() ? "" : " WHERE " + columna + " >= ? AND " + columna + " < ?\n";
    }

    private Indicador indicador(String nombre, long valor) {
        return new Indicador(nombre, String.valueOf(valor));
    }

    private String campoCsv(String valor) {
        String campo = valor == null ? "" : valor;
        if (!campo.isEmpty() && "=+-@".indexOf(campo.charAt(0)) >= 0) {
            campo = "'" + campo;
        }
        if (campo.indexOf(';') >= 0 || campo.indexOf(',') >= 0 || campo.indexOf('"') >= 0 || campo.indexOf('\n') >= 0 || campo.indexOf('\r') >= 0) {
            return '"' + campo.replace("\"", "\"\"") + '"';
        }
        return campo;
    }

    private String escaparXml(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String celdaDatoXml(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "<Data ss:Type=\"String\"></Data>";
        }
        String limpio = valor.trim();
        if (limpio.matches("^-?\\d+(\\.\\d+)?$")) {
            return "<Data ss:Type=\"Number\">" + limpio + "</Data>";
        }
        return "<Data ss:Type=\"String\">" + escaparXml(valor) + "</Data>";
    }

    private String limpiarNombreHoja(String nombre, int fallbackIndex) {
        if (nombre == null || nombre.isBlank()) return "Datos " + fallbackIndex;
        String limpio = nombre.replaceAll("[:\\\\/?*\\[\\]]", " ").trim();
        if (limpio.length() > 28) {
            limpio = limpio.substring(0, 28);
        }
        return limpio;
    }

    private List<String> padRow(List<String> fila, int columnas) {
        List<String> resultado = new ArrayList<>(fila);
        while (resultado.size() < columnas) {
            resultado.add("");
        }
        return resultado;
    }

    private List<Float> calcularAnchosProporcionales(List<String> columnas, List<List<String>> filas) {
        int n = columnas.size();
        float[] pesos = new float[n];
        for (int i = 0; i < n; i++) {
            int maxLen = columnas.get(i).length();
            int sampleLimit = Math.min(filas.size(), 35);
            for (int r = 0; r < sampleLimit; r++) {
                List<String> fila = filas.get(r);
                if (i < fila.size()) {
                    maxLen = Math.max(maxLen, fila.get(i).length());
                }
            }
            pesos[i] = Math.max(7, Math.min(38, maxLen));
        }
        float sumaPesos = 0;
        for (float p : pesos) sumaPesos += p;

        float anchoUtil = 511f;
        List<Float> anchos = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            float w = (pesos[i] / sumaPesos) * anchoUtil;
            anchos.add(Math.max(40f, w));
        }
        float sumaReal = 0;
        for (float w : anchos) sumaReal += w;
        float factor = anchoUtil / sumaReal;
        for (int i = 0; i < n; i++) {
            anchos.set(i, anchos.get(i) * factor);
        }
        return anchos;
    }

    private List<Float> calcularXOffsets(List<Float> anchos) {
        List<Float> offsets = new ArrayList<>();
        float x = 42f;
        for (Float w : anchos) {
            offsets.add(x);
            x += w;
        }
        return offsets;
    }

    private String truncarTexto(String valor, float anchoCol) {
        if (valor == null) return "";
        int maxChars = Math.max(3, (int) ((anchoCol - 8f) / 4.3f));
        if (valor.length() > maxChars) {
            return valor.substring(0, Math.max(1, maxChars - 2)) + "..";
        }
        return valor;
    }

    private byte[] construirPdf(List<LineaPdf> lineas, Reporte reporte) {
        List<List<LineaPdf>> paginas = paginar(lineas);
        List<byte[]> contenidos = new ArrayList<>();
        for (int indice = 0; indice < paginas.size(); indice++) {
            contenidos.add(contenidoPagina(paginas.get(indice), indice + 1, paginas.size(), reporte));
        }

        int objetos = 6 + contenidos.size() * 2;
        List<Integer> offsets = new ArrayList<>(Collections.nCopies(objetos + 1, 0));
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        escribirAscii(salida, "%PDF-1.4\n%");
        salida.writeBytes(new byte[]{(byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n'});

        escribirObjeto(salida, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder paginasReferencias = new StringBuilder();
        for (int indice = 0; indice < contenidos.size(); indice++) {
            paginasReferencias.append(7 + indice * 2).append(" 0 R ");
        }
        escribirObjeto(salida, offsets, 2,
                "<< /Type /Pages /Kids [" + paginasReferencias + "] /Count " + contenidos.size() + " >>");
        escribirObjeto(salida, offsets, 3,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        escribirObjeto(salida, offsets, 4,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        escribirObjeto(salida, offsets, 5,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Oblique /Encoding /WinAnsiEncoding >>");
        escribirObjeto(salida, offsets, 6,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-BoldOblique /Encoding /WinAnsiEncoding >>");

        for (int indice = 0; indice < contenidos.size(); indice++) {
            int pagina = 7 + indice * 2;
            int contenido = pagina + 1;
            escribirObjeto(salida, offsets, pagina,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                            + "/Resources << /Font << /F1 3 0 R /F2 4 0 R /F3 5 0 R /F4 6 0 R >> >> "
                            + "/Contents " + contenido + " 0 R >>");
            escribirStream(salida, offsets, contenido, contenidos.get(indice));
        }

        int xref = salida.size();
        escribirAscii(salida, "xref\n0 " + (objetos + 1) + "\n");
        escribirAscii(salida, "0000000000 65535 f \n");
        for (int indice = 1; indice <= objetos; indice++) {
            escribirAscii(salida, String.format(Locale.ROOT, "%010d 00000 n \n", offsets.get(indice)));
        }
        escribirAscii(salida, "trailer\n<< /Size " + (objetos + 1) + " /Root 1 0 R >>\nstartxref\n"
                + xref + "\n%%EOF\n");
        return salida.toByteArray();
    }

    private List<List<LineaPdf>> paginar(List<LineaPdf> lineas) {
        List<List<LineaPdf>> paginas = new ArrayList<>();
        List<LineaPdf> actual = new ArrayList<>();
        int posicionY = 700;

        for (LineaPdf linea : lineas) {
            int alto = linea.altoDeseado();
            if (posicionY - alto < 48 && !actual.isEmpty()) {
                paginas.add(actual);
                actual = new ArrayList<>();
                posicionY = 770;
            }
            actual.add(linea);
            posicionY -= alto;
        }
        if (!actual.isEmpty() || paginas.isEmpty()) {
            paginas.add(actual);
        }
        return paginas;
    }

    private byte[] contenidoPagina(List<LineaPdf> lineas, int numeroPagina, int totalPaginas, Reporte reporte) {
        StringBuilder contenido = new StringBuilder();

        if (numeroPagina == 1) {
            // Header Banner Principal (Página 1)
            contenido.append("0.000 0.353 0.169 rg\n42 755 511 48 re f\n");
            contenido.append("0.100 0.650 0.350 rg\n42 752 511 3 re f\n");
            contenido.append("BT\n1 1 1 rg\n/F2 15 Tf\n1 0 0 1 54 782 Tm\n(MAPS CONNECT) Tj\n");
            contenido.append("/F1 7.5 Tf\n1 0 0 1 54 768 Tm\n(PLATAFORMA ACADEMICA Y DE MENTORIA - TECMILENIO) Tj\nET\n");

            // Pastilla del Periodo a la derecha
            contenido.append("1 1 1 rg\n405 764 140 26 re f\n");
            contenido.append("BT\n0.35 0.40 0.45 rg\n/F2 6 Tf\n1 0 0 1 413 780 Tm\n(PERIODO ACADEMICO) Tj\n");
            contenido.append("0.000 0.353 0.169 rg\n/F2 8 Tf\n1 0 0 1 413 769 Tm\n(")
                    .append(escaparPdf(reporte.periodo())).append(") Tj\nET\n");

            // Título del reporte y metadatos
            contenido.append("BT\n0.12 0.16 0.22 rg\n/F2 12 Tf\n1 0 0 1 42 735 Tm\n(")
                    .append(escaparPdf(reporte.titulo())).append(") Tj\n");
            contenido.append("0.45 0.50 0.55 rg\n/F1 7.5 Tf\n1 0 0 1 42 722 Tm\n(Emitido el: ")
                    .append(FECHA_HORA.format(reporte.generadoEn())).append(" - Documento Institucional Oficial) Tj\nET\n");
            contenido.append("0.88 0.90 0.92 RG\n0.5 w\n42 714 m 553 714 l S\n");
        } else {
            // Header Compacto (Páginas posteriores)
            contenido.append("0.000 0.353 0.169 rg\n42 788 511 26 re f\n");
            contenido.append("BT\n1 1 1 rg\n/F2 8.5 Tf\n1 0 0 1 52 797 Tm\n(MAPS CONNECT - ")
                    .append(escaparPdf(reporte.titulo())).append(") Tj\n");
            contenido.append("/F1 7.5 Tf\n1 0 0 1 420 797 Tm\n(")
                    .append(escaparPdf(reporte.periodo())).append(") Tj\nET\n");
        }

        // Footer en todas las páginas
        contenido.append("0.88 0.90 0.92 RG\n0.5 w\n42 32 m 553 32 l S\n");
        contenido.append("BT\n0.50 0.55 0.60 rg\n/F1 6.5 Tf\n1 0 0 1 42 22 Tm\n(Documento Institucional Oficial - Plataforma MAPS Connect - Tecnologico Tecmilenio) Tj\n");
        contenido.append("/F2 7 Tf\n1 0 0 1 490 22 Tm\n(Pagina ").append(numeroPagina).append(" de ").append(totalPaginas).append(") Tj\nET\n");

        int posicionY = numeroPagina == 1 ? 700 : 770;

        for (LineaPdf linea : lineas) {
            switch (linea.tipo()) {
                case TARJETAS_KPI -> {
                    List<Indicador> inds = linea.indicadores();
                    int total = inds.size();
                    int cols = Math.min(3, Math.max(2, total));
                    float gap = 8f;
                    float totalW = 511f;
                    float cardW = (totalW - (cols - 1) * gap) / cols;
                    float cardH = 38f;

                    for (int i = 0; i < total; i++) {
                        int row = i / cols;
                        int col = i % cols;
                        float cx = 42f + col * (cardW + gap);
                        float cy = posicionY - (row + 1) * cardH - (row * 6f);

                        // Card background
                        contenido.append(String.format(Locale.ROOT, "0.960 0.970 0.985 rg\n%.1f %.1f %.1f %.1f re f\n",
                                cx, cy, cardW, cardH));
                        // Card border
                        contenido.append(String.format(Locale.ROOT, "0.850 0.880 0.910 RG\n0.5 w\n%.1f %.1f %.1f %.1f re S\n",
                                cx, cy, cardW, cardH));
                        // Top accent bar
                        contenido.append(String.format(Locale.ROOT, "0.000 0.353 0.169 rg\n%.1f %.1f %.1f 2.5 re f\n",
                                cx, cy + cardH - 2.5f, cardW));

                        Indicador ind = inds.get(i);
                        // Value text
                        contenido.append(String.format(Locale.ROOT, "BT\n0.000 0.353 0.169 rg\n/F2 12 Tf\n1 0 0 1 %.1f %.1f Tm\n(%s) Tj\n",
                                cx + 8f, cy + 19f, escaparPdf(ind.valor())));
                        // Label text (truncated if too long)
                        String lbl = ind.nombre().toUpperCase(Locale.ROOT);
                        int maxLbl = (int) ((cardW - 16f) / 3.8f);
                        if (lbl.length() > maxLbl) {
                            lbl = lbl.substring(0, Math.max(1, maxLbl - 2)) + "..";
                        }
                        contenido.append(String.format(Locale.ROOT, "0.320 0.380 0.440 rg\n/F1 6.5 Tf\n1 0 0 1 %.1f %.1f Tm\n(%s) Tj\nET\n",
                                cx + 8f, cy + 8f, escaparPdf(lbl)));
                    }
                }
                case SECCION_TITULO -> {
                    contenido.append(String.format(Locale.ROOT, "0.000 0.353 0.169 rg\n42 %.1f 3.5 12 re f\n", (float) (posicionY - 10)));
                    contenido.append(String.format(Locale.ROOT, "BT\n0.10 0.14 0.20 rg\n/F2 10.5 Tf\n1 0 0 1 50 %d Tm\n(%s) Tj\nET\n",
                            posicionY - 8, escaparPdf(linea.texto())));
                }
                case NOTA -> {
                    contenido.append(String.format(Locale.ROOT, "1.00 0.985 0.92 rg\n42 %d 511 16 re f\n", posicionY - 14));
                    contenido.append(String.format(Locale.ROOT, "0.90 0.82 0.58 RG\n0.5 w\n42 %d 511 16 re S\n", posicionY - 14));
                    contenido.append(String.format(Locale.ROOT, "BT\n0.55 0.35 0.05 rg\n/F3 7.5 Tf\n1 0 0 1 50 %d Tm\n(Nota: %s) Tj\nET\n",
                            posicionY - 10, escaparPdf(linea.texto())));
                }
                case TABLA_ENCABEZADO -> {
                    contenido.append(String.format(Locale.ROOT, "0.000 0.300 0.150 rg\n42 %d 511 16 re f\n", posicionY - 13));
                    contenido.append("BT\n1 1 1 rg\n/F2 7.5 Tf\n");
                    for (int i = 0; i < linea.celdas().size(); i++) {
                        float x = linea.xOffset().get(i) + 4;
                        float w = linea.anchosCol().get(i);
                        String celdaTruncada = truncarTexto(linea.celdas().get(i), w);
                        contenido.append(String.format(Locale.ROOT, "1 0 0 1 %.1f %d Tm\n", x, posicionY - 9));
                        contenido.append('(').append(escaparPdf(celdaTruncada)).append(") Tj\n");
                    }
                    contenido.append("ET\n");
                }
                case TABLA_FILA -> {
                    if (linea.esPar()) {
                        contenido.append(String.format(Locale.ROOT, "1 1 1 rg\n42 %d 511 14 re f\n", posicionY - 11));
                    } else {
                        contenido.append(String.format(Locale.ROOT, "0.965 0.975 0.985 rg\n42 %d 511 14 re f\n", posicionY - 11));
                    }
                    contenido.append(String.format(Locale.ROOT, "0.90 0.91 0.93 RG\n0.5 w\n42 %d m 553 %d l S\n", posicionY - 11, posicionY - 11));
                    contenido.append("BT\n0.15 0.18 0.22 rg\n/F1 7.5 Tf\n");
                    for (int i = 0; i < linea.celdas().size(); i++) {
                        float x = linea.xOffset().get(i) + 4;
                        float w = linea.anchosCol().get(i);
                        String celdaTruncada = truncarTexto(linea.celdas().get(i), w);
                        contenido.append(String.format(Locale.ROOT, "1 0 0 1 %.1f %d Tm\n", x, posicionY - 8));
                        contenido.append('(').append(escaparPdf(celdaTruncada)).append(") Tj\n");
                    }
                    contenido.append("ET\n");
                }
                case TEXTO_SIMPLE -> {
                    String f = linea.esPar() ? "/F2" : "/F1";
                    contenido.append(String.format(Locale.ROOT, "BT\n0.20 0.25 0.30 rg\n%s %d Tf\n1 0 0 1 42 %d Tm\n(%s) Tj\nET\n",
                            f, linea.tamano(), posicionY - 8, escaparPdf(linea.texto())));
                }
                case ESPACIO -> {
                    // Sin dibujo visual, solo mueve posicionY
                }
            }
            posicionY -= linea.altoDeseado();
        }

        return contenido.toString().getBytes(WINDOWS_1252);
    }

    private String escaparPdf(String valor) {
        if (valor == null || valor.isEmpty()) return "";
        StringBuilder resultado = new StringBuilder();
        for (int indice = 0; indice < valor.length(); indice++) {
            char caracter = valor.charAt(indice);
            if (caracter == '\\' || caracter == '(' || caracter == ')') {
                resultado.append('\\').append(caracter);
            } else if (caracter == '\u2013' || caracter == '\u2014') {
                resultado.append('-');
            } else if (caracter == '\u2018' || caracter == '\u2019') {
                resultado.append('\'');
            } else if (caracter == '\u201C' || caracter == '\u201D') {
                resultado.append('"');
            } else if (caracter == '\u2026') {
                resultado.append("...");
            } else if (caracter == '\r' || caracter == '\n') {
                resultado.append(' ');
            } else if (caracter >= 32 && caracter <= 126) {
                resultado.append(caracter);
            } else if (caracter >= 160 && caracter <= 255) {
                resultado.append(caracter);
            } else {
                resultado.append(' ');
            }
        }
        return resultado.toString();
    }

    private void escribirObjeto(ByteArrayOutputStream salida, List<Integer> offsets, int numero, String contenido) {
        offsets.set(numero, salida.size());
        escribirAscii(salida, numero + " 0 obj\n");
        salida.writeBytes(contenido.getBytes(WINDOWS_1252));
        escribirAscii(salida, "\nendobj\n");
    }

    private void escribirStream(ByteArrayOutputStream salida, List<Integer> offsets, int numero, byte[] contenido) {
        offsets.set(numero, salida.size());
        escribirAscii(salida, numero + " 0 obj\n<< /Length " + contenido.length + " >>\nstream\n");
        salida.writeBytes(contenido);
        escribirAscii(salida, "\nendstream\nendobj\n");
    }

    private void escribirAscii(ByteArrayOutputStream salida, String texto) {
        salida.writeBytes(texto.getBytes(StandardCharsets.US_ASCII));
    }

    private String fecha(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof LocalDate fecha) return FECHA.format(fecha);
        if (valor instanceof LocalDateTime fecha) return FECHA.format(fecha.toLocalDate());
        if (valor instanceof java.sql.Date fecha) return FECHA.format(fecha.toLocalDate());
        if (valor instanceof java.sql.Timestamp fecha) return FECHA.format(fecha.toLocalDateTime().toLocalDate());
        return valor == null ? "" : String.valueOf(valor);
    }

    private String fechaHora(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof LocalDateTime fecha) return FECHA_HORA.format(fecha);
        if (valor instanceof java.sql.Timestamp fecha) return FECHA_HORA.format(fecha.toLocalDateTime());
        if (valor instanceof LocalDate fecha) return FECHA.format(fecha);
        if (valor instanceof java.sql.Date fecha) return FECHA.format(fecha.toLocalDate());
        return valor == null ? "" : String.valueOf(valor);
    }

    private String hora(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof java.sql.Time hora) return hora.toLocalTime().toString();
        if (valor instanceof java.time.LocalTime hora) return hora.toString();
        return valor == null ? "" : String.valueOf(valor);
    }

    private long numero(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof Number numero) return numero.longValue();
        if (valor == null || String.valueOf(valor).isBlank()) return 0;
        try {
            return Long.parseLong(String.valueOf(valor));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String decimal(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof Number numero) return String.format(Locale.ROOT, "%.2f", numero.doubleValue());
        return "0.00";
    }

    private boolean booleano(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        if (valor instanceof Boolean booleano) return booleano;
        if (valor instanceof Number numero) return numero.intValue() != 0;
        return "true".equalsIgnoreCase(String.valueOf(valor)) || "1".equals(String.valueOf(valor));
    }

    private String texto(Map<String, Object> fila, String columna) {
        Object valor = valor(fila, columna);
        return valor == null || String.valueOf(valor).isBlank() ? "Sin dato" : String.valueOf(valor).trim();
    }

    private String textoConVacio(Map<String, Object> fila, String columna, String respaldo) {
        Object valor = valor(fila, columna);
        return valor == null || String.valueOf(valor).isBlank() ? respaldo : String.valueOf(valor).trim();
    }

    private Object valor(Map<String, Object> fila, String columna) {
        Object directo = fila.get(columna);
        if (directo != null || fila.containsKey(columna)) return directo;
        for (Map.Entry<String, Object> entrada : fila.entrySet()) {
            if (entrada.getKey().equalsIgnoreCase(columna)) return entrada.getValue();
        }
        return null;
    }

    private String porcentaje(long numerador, long denominador) {
        if (denominador == 0) return "0.0%";
        return String.format(Locale.ROOT, "%.1f%%", (numerador * 100.0) / denominador);
    }

    private String promedio(long suma, long cantidad) {
        if (cantidad == 0) return "0.00";
        return String.format(Locale.ROOT, "%.2f", suma / (double) cantidad);
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank() || "Sin dato".equals(valor)) return "Sin dato";
        return valor.substring(0, 1).toUpperCase(Locale.ROOT) + valor.substring(1).toLowerCase(Locale.ROOT);
    }

    private String siNo(boolean condicion) {
        return condicion ? "Sí" : "No";
    }

    public record Reporte(String codigo, String titulo, String periodo, String cicloArchivo,
                          LocalDateTime generadoEn, List<Indicador> indicadores,
                          List<Seccion> secciones, List<String> notas) {
    }

    public record Indicador(String nombre, String valor) {
    }

    public record Seccion(String titulo, List<String> columnas, List<List<String>> filas) {
    }

    private enum TipoReporte {
        CIRCULOS("circulos", "Participación en círculos de estudio e inscripciones"),
        DUDAS("dudas", "Métricas de resolución de dudas por materia"),
        RECURSOS("recursos", "Uso y moderación del repositorio de apuntes"),
        EMPRESAS("empresas", "Evaluación de empresas vinculadas");

        private final String codigo;
        private final String titulo;

        TipoReporte(String codigo, String titulo) {
            this.codigo = codigo;
            this.titulo = titulo;
        }

        private static TipoReporte desde(String solicitado) {
            String normalizado = Normalizer.normalize(solicitado == null ? "" : solicitado.trim(), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .toLowerCase(Locale.ROOT);
            for (TipoReporte tipo : values()) {
                if (tipo.codigo.equals(normalizado)) return tipo;
            }
            throw new IllegalArgumentException("Tipo de reporte no válido");
        }
    }

    private record Periodo(LocalDate inicio, LocalDate finExclusivo, String etiqueta,
                           String codigoArchivo, boolean todos) {
        private static Periodo desde(String solicitado) {
            String ciclo = solicitado == null || solicitado.isBlank()
                    ? "current" : solicitado.trim().toLowerCase(Locale.ROOT);
            if ("all".equals(ciclo)) {
                return new Periodo(null, null, "Histórico completo", "historico", true);
            }

            int anio;
            int semestre;
            if ("current".equals(ciclo)) {
                LocalDate hoy = LocalDate.now();
                anio = hoy.getYear();
                semestre = hoy.getMonthValue() <= 6 ? 1 : 2;
            } else {
                String[] partes = ciclo.split("-");
                if (partes.length != 2) {
                    throw new IllegalArgumentException("Ciclo no válido. Usa AAAA-1, AAAA-2, current o all");
                }
                try {
                    anio = Integer.parseInt(partes[0]);
                    semestre = Integer.parseInt(partes[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Ciclo no válido. Usa AAAA-1 o AAAA-2");
                }
            }
            if (anio < 2020 || anio > 2100 || (semestre != 1 && semestre != 2)) {
                throw new IllegalArgumentException("Ciclo no válido. Usa AAAA-1 o AAAA-2");
            }

            LocalDate inicio = semestre == 1 ? LocalDate.of(anio, 1, 1) : LocalDate.of(anio, 8, 1);
            LocalDate fin = semestre == 1 ? LocalDate.of(anio, 6, 1) : LocalDate.of(anio + 1, 1, 1);
            String meses = semestre == 1 ? "Enero - Mayo" : "Agosto - Diciembre";
            return new Periodo(inicio, fin, anio + "-" + semestre + " (" + meses + ")",
                    anio + "-" + semestre, false);
        }
    }

    private enum TipoLineaPdf {
        TARJETAS_KPI,
        SECCION_TITULO,
        TABLA_ENCABEZADO,
        TABLA_FILA,
        NOTA,
        TEXTO_SIMPLE,
        ESPACIO
    }

    private record LineaPdf(
            TipoLineaPdf tipo,
            String texto,
            int tamano,
            List<String> celdas,
            List<Float> xOffset,
            List<Float> anchosCol,
            boolean esPar,
            List<Indicador> indicadores,
            int altoDeseado
    ) {
        static LineaPdf tarjetasKpi(List<Indicador> indicadores) {
            int rows = (int) Math.ceil(indicadores.size() / 3.0);
            int alto = rows * 44 + 8;
            return new LineaPdf(TipoLineaPdf.TARJETAS_KPI, null, 0, null, null, null, false, indicadores, alto);
        }

        static LineaPdf seccionTitulo(String titulo) {
            return new LineaPdf(TipoLineaPdf.SECCION_TITULO, titulo, 11, null, null, null, false, null, 24);
        }

        static LineaPdf nota(String nota) {
            return new LineaPdf(TipoLineaPdf.NOTA, nota, 8, null, null, null, false, null, 22);
        }

        static LineaPdf tablaEncabezado(List<String> celdas, List<Float> xOffset, List<Float> anchosCol) {
            return new LineaPdf(TipoLineaPdf.TABLA_ENCABEZADO, null, 8, celdas, xOffset, anchosCol, false, null, 18);
        }

        static LineaPdf tablaFila(List<String> celdas, List<Float> xOffset, List<Float> anchosCol, boolean esPar) {
            return new LineaPdf(TipoLineaPdf.TABLA_FILA, null, 8, celdas, xOffset, anchosCol, esPar, null, 15);
        }

        static LineaPdf texto(String texto, boolean negritas, int tamano) {
            return new LineaPdf(TipoLineaPdf.TEXTO_SIMPLE, texto, tamano, null, null, null, negritas, null, tamano >= 12 ? 18 : 14);
        }

        static LineaPdf espacio(int alto) {
            return new LineaPdf(TipoLineaPdf.ESPACIO, null, 0, null, null, null, false, null, alto);
        }
    }

    private static final class ResumenMateria {
        private long publicaciones;
        private long resueltas;
        private long respuestas;
    }

    private static final class ResumenRecurso {
        private long recursos;
        private long descargas;
        private long reportes;
        private long ocultos;
    }
}
