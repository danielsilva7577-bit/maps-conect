package com.tecmilenio.mapsconect.storage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Contrato de servicio de almacenamiento de archivos.
 *
 * <p>Abstrae la persistencia física de archivos (apuntes, fotos, adjuntos de chat)
 * permitiendo cambiar de almacenamiento local en disco a almacenamiento de objetos
 * en la nube (AWS S3, MinIO, Cloudflare R2) sin modificar los controladores.</p>
 */
public interface AlmacenamientoService {

    /**
     * Guarda el contenido de un archivo en la ruta especificada.
     *
     * @param rutaRelativa ruta del objeto (ej. "recursos/14", "adjuntos/25")
     * @param contenido    bytes del archivo
     * @param contentType  tipo MIME (ej. "application/pdf")
     * @throws IOException si ocurre un error de E/S o de red
     */
    void guardar(String rutaRelativa, byte[] contenido, String contentType) throws IOException;

    /**
     * Guarda el contenido desde un stream de entrada.
     *
     * @param rutaRelativa ruta del objeto
     * @param stream       flujo de bytes
     * @param tamanoBytes  tamaño en bytes
     * @param contentType  tipo MIME
     * @throws IOException si ocurre un error de E/S o de red
     */
    void guardar(String rutaRelativa, InputStream stream, long tamanoBytes, String contentType) throws IOException;

    /**
     * Descarga y obtiene los bytes del archivo en la ruta especificada.
     *
     * @param rutaRelativa ruta del objeto
     * @return arreglo de bytes del archivo
     * @throws IOException si el archivo no existe o no se puede leer
     */
    byte[] descargar(String rutaRelativa) throws IOException;

    /**
     * Elimina el archivo de la ruta especificada.
     *
     * @param rutaRelativa ruta del objeto
     * @throws IOException si no se puede eliminar
     */
    void eliminar(String rutaRelativa) throws IOException;

    /**
     * Verifica si el archivo existe en el almacenamiento.
     *
     * @param rutaRelativa ruta del objeto
     * @return true si existe, false si no
     */
    boolean existe(String rutaRelativa);

    /**
     * Devuelve el nombre del proveedor activo ("LOCAL" o "S3").
     *
     * @return nombre del proveedor
     */
    String getTipoAlmacenamiento();

}
