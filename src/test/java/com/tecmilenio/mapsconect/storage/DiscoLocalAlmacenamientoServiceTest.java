package com.tecmilenio.mapsconect.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscoLocalAlmacenamientoServiceTest {

    @TempDir
    Path tempDir;

    private DiscoLocalAlmacenamientoService storageService;

    @BeforeEach
    void setUp() {
        storageService = new DiscoLocalAlmacenamientoService(tempDir.toString());
    }

    @Test
    void guardarYDescargarBytes_correcto() throws IOException {
        String ruta = "test/archivo.txt";
        byte[] contenido = "Hola MAPS Connect".getBytes();

        storageService.guardar(ruta, contenido, "text/plain");
        assertThat(storageService.existe(ruta)).isTrue();

        byte[] descargado = storageService.descargar(ruta);
        assertThat(descargado).isEqualTo(contenido);
    }

    @Test
    void guardarInputStream_correcto() throws IOException {
        String ruta = "recursos/doc.pdf";
        byte[] contenido = "%PDF-1.4 mock content".getBytes();

        storageService.guardar(ruta, new ByteArrayInputStream(contenido), contenido.length, "application/pdf");
        assertThat(storageService.existe(ruta)).isTrue();

        byte[] descargado = storageService.descargar(ruta);
        assertThat(descargado).isEqualTo(contenido);
    }

    @Test
    void eliminar_borraArchivo() throws IOException {
        String ruta = "adjuntos/1.jpg";
        storageService.guardar(ruta, "imagen".getBytes(), "image/jpeg");
        assertThat(storageService.existe(ruta)).isTrue();

        storageService.eliminar(ruta);
        assertThat(storageService.existe(ruta)).isFalse();
    }

    @Test
    void descargarInexistente_lanzaFileNotFoundException() {
        assertThatThrownBy(() -> storageService.descargar("no_existe.txt"))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    void pathTraversal_lanzaExcepcion() {
        assertThatThrownBy(() -> storageService.guardar("../malicioso.txt", "evil".getBytes(), "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
