package com.tecmilenio.mapsconect.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validación segura de archivos subidos por usuarios.
 * Restringe extensiones a una lista blanca y sanea nombres para evitar
 * inyección de cabeceras HTTP o rutas peligrosas.
 */
public final class ArchivoSeguro {

    private ArchivoSeguro() {
    }

    private static final Set<String> EXTENSIONES_PERMITIDAS = new HashSet<>(Arrays.asList(
            "pdf",
            "doc", "docx", "odt",
            "ppt", "pptx", "odp",
            "xls", "xlsx", "ods", "csv",
            "txt", "md", "rtf",
            "zip", "rar", "7z",
            "png", "jpg", "jpeg", "gif", "webp",
            "mp4", "mp3", "wav"
    ));

    private static final long MAX_BYTES = 20L * 1024 * 1024;

    public static void validar(String nombreOriginal, long tamanoBytes) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new IllegalArgumentException("El archivo debe tener nombre");
        }
        if (tamanoBytes <= 0) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        if (tamanoBytes > MAX_BYTES) {
            throw new IllegalArgumentException("El archivo no puede superar los 20 MB");
        }
        String ext = extension(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido (." + (ext.isEmpty() ? "sin extensión" : ext) + "). "
                            + "Extensiones permitidas: pdf, doc, xls, ppt, txt, imágenes, zip, audio y video.");
        }
    }

    public static String limpiarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "archivo";
        }
        String base = Paths.get(nombre).getFileName().toString().replace("\\", "").trim();
        if (base.isBlank()) {
            base = "archivo";
        }
        return base;
    }

    public static String mimeDesdeNombre(String nombre) {
        switch (extension(nombre)) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "odt": return "application/vnd.oasis.opendocument.text";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "odp": return "application/vnd.oasis.opendocument.presentation";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ods": return "application/vnd.oasis.opendocument.spreadsheet";
            case "csv": return "text/csv";
            case "txt": return "text/plain";
            case "md": return "text/markdown";
            case "rtf": return "application/rtf";
            case "zip": return "application/zip";
            case "rar": return "application/vnd.rar";
            case "7z": return "application/x-7z-compressed";
            case "png": return "image/png";
            case "jpg": return "image/jpeg";
            case "jpeg": return "image/jpeg";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            default: return "application/octet-stream";
        }
    }

    public static String encabezadoDescargaSeguro(String nombreOriginal) {
        String limpio = limpiarNombre(nombreOriginal);
        // Elimina caracteres que permitan inyección de cabeceras HTTP.
        limpio = limpio.replaceAll("[\\r\\n\"\u0000-\u001F;]", "_");
        return "attachment; filename=\"" + limpio + "\"";
    }

    public static String extension(String nombre) {
        if (nombre == null) return "";
        int idx = nombre.lastIndexOf('.');
        return idx >= 0 ? nombre.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }
}
