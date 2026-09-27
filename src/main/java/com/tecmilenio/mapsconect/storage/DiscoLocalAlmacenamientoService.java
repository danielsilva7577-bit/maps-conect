package com.tecmilenio.mapsconect.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Implementación de almacenamiento en el sistema de archivos local del servidor.
 *
 * <p>Usado por defecto en desarrollo local y despliegues en un solo nodo.</p>
 */
@Service("discoLocalAlmacenamientoService")
public class DiscoLocalAlmacenamientoService implements AlmacenamientoService {

    private static final Logger log = LoggerFactory.getLogger(DiscoLocalAlmacenamientoService.class);

    private final Path baseDir;

    public DiscoLocalAlmacenamientoService(
            @Value("${app.storage.local.dir:uploads}") String localDir) {
        Path p = Paths.get(localDir);
        this.baseDir = p.isAbsolute() ? p.normalize() : Paths.get(System.getProperty("user.dir"), localDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
            Files.createDirectories(this.baseDir.resolve("recursos"));
            Files.createDirectories(this.baseDir.resolve("adjuntos"));
            log.info("[Storage] Almacenamiento local inicializado en {}", this.baseDir);
        } catch (IOException e) {
            log.error("[Storage] Error al inicializar directorios de almacenamiento local: {}", e.getMessage());
        }
    }

    @Override
    public void guardar(String rutaRelativa, byte[] contenido, String contentType) throws IOException {
        Path destino = resolverRuta(rutaRelativa);
        Files.createDirectories(destino.getParent());
        Files.write(destino, contenido);
    }

    @Override
    public void guardar(String rutaRelativa, InputStream stream, long tamanoBytes, String contentType) throws IOException {
        Path destino = resolverRuta(rutaRelativa);
        Files.createDirectories(destino.getParent());
        Files.copy(stream, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public byte[] descargar(String rutaRelativa) throws IOException {
        Path origen = resolverRuta(rutaRelativa);
        if (!Files.exists(origen) || !Files.isRegularFile(origen)) {
            throw new FileNotFoundException("Archivo no encontrado: " + rutaRelativa);
        }
        return Files.readAllBytes(origen);
    }

    @Override
    public void eliminar(String rutaRelativa) throws IOException {
        Path destino = resolverRuta(rutaRelativa);
        Files.deleteIfExists(destino);
    }

    @Override
    public boolean existe(String rutaRelativa) {
        Path destino = resolverRuta(rutaRelativa);
        return Files.exists(destino) && Files.isRegularFile(destino);
    }

    @Override
    public String getTipoAlmacenamiento() {
        return "LOCAL";
    }

    private Path resolverRuta(String rutaRelativa) {
        String normalizada = (rutaRelativa == null ? "" : rutaRelativa.replace("\\", "/").trim());
        while (normalizada.startsWith("/")) {
            normalizada = normalizada.substring(1);
        }
        Path res = baseDir.resolve(normalizada).normalize();
        // Protección estricta contra Path Traversal:
        if (!res.startsWith(baseDir)) {
            throw new IllegalArgumentException("Ruta no permitida fuera del directorio base: " + rutaRelativa);
        }
        return res;
    }

}
