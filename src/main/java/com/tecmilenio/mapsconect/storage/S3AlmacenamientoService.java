package com.tecmilenio.mapsconect.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementación de almacenamiento en nube compatible con S3 (AWS S3, MinIO, Cloudflare R2).
 *
 * <p>Permite a múltiples instancias del backend compartir un repositorio centralizado de
 * archivos sin colisiones ni fallos 404 entre nodos.</p>
 */
@Service("s3AlmacenamientoService")
public class S3AlmacenamientoService implements AlmacenamientoService {

    private static final Logger log = LoggerFactory.getLogger(S3AlmacenamientoService.class);
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final String endpoint;
    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final HttpClient httpClient;

    public S3AlmacenamientoService(
            @Value("${app.storage.s3.endpoint:https://s3.amazonaws.com}") String endpoint,
            @Value("${app.storage.s3.bucket:maps-conect-storage}") String bucket,
            @Value("${app.storage.s3.region:us-east-1}") String region,
            @Value("${app.storage.s3.access-key:}") String accessKey,
            @Value("${app.storage.s3.secret-key:}") String secretKey) {
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        log.info("[Storage] S3 Almacenamiento configurado para bucket='{}', endpoint='{}', region='{}'",
                bucket, this.endpoint, region);
    }

    @Override
    public void guardar(String rutaRelativa, byte[] contenido, String contentType) throws IOException {
        String key = limpiarKey(rutaRelativa);
        URI uri = construirUri(key);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(contenido))
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream");

            firmarYSolicitar(builder, "PUT", key, contenido, contentType);
            HttpResponse<Void> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());

            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IOException("Error subiendo objeto a S3 [" + key + "]: HTTP " + resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida subiendo archivo a S3", e);
        }
    }

    @Override
    public void guardar(String rutaRelativa, InputStream stream, long tamanoBytes, String contentType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stream.transferTo(baos);
        guardar(rutaRelativa, baos.toByteArray(), contentType);
    }

    @Override
    public byte[] descargar(String rutaRelativa) throws IOException {
        String key = limpiarKey(rutaRelativa);
        URI uri = construirUri(key);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .GET();

            firmarYSolicitar(builder, "GET", key, new byte[0], null);
            HttpResponse<byte[]> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());

            if (resp.statusCode() == 404) {
                throw new FileNotFoundException("Objeto no encontrado en S3: " + key);
            }
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IOException("Error descargando objeto de S3 [" + key + "]: HTTP " + resp.statusCode());
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida descargando archivo de S3", e);
        }
    }

    @Override
    public void eliminar(String rutaRelativa) throws IOException {
        String key = limpiarKey(rutaRelativa);
        URI uri = construirUri(key);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .DELETE();

            firmarYSolicitar(builder, "DELETE", key, new byte[0], null);
            HttpResponse<Void> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());

            if (resp.statusCode() != 204 && resp.statusCode() != 200 && resp.statusCode() != 404) {
                throw new IOException("Error eliminando objeto en S3 [" + key + "]: HTTP " + resp.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Operación interrumpida eliminando archivo en S3", e);
        }
    }

    @Override
    public boolean existe(String rutaRelativa) {
        String key = limpiarKey(rutaRelativa);
        URI uri = construirUri(key);

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(10))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody());

            firmarYSolicitar(builder, "HEAD", key, new byte[0], null);
            HttpResponse<Void> resp = httpClient.send(builder.build(), HttpResponse.BodyHandlers.discarding());
            return resp.statusCode() >= 200 && resp.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getTipoAlmacenamiento() {
        return "S3";
    }

    private String limpiarKey(String ruta) {
        if (ruta == null) return "";
        String k = ruta.replace("\\", "/").trim();
        while (k.startsWith("/")) k = k.substring(1);
        return k;
    }

    private URI construirUri(String key) {
        // Path-style: endpoint/bucket/key
        return URI.create(endpoint + "/" + bucket + "/" + key);
    }

    private void firmarYSolicitar(HttpRequest.Builder builder, String method, String key, byte[] payload, String contentType) {
        // Firma básica AWS Signature V4 o Basic Auth (compatible con MinIO/R2/S3)
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String amzDate = now.format(ISO_DATE_FORMAT);
        String dateOnly = now.format(DATE_ONLY_FORMAT);

        builder.header("x-amz-date", amzDate);

        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            try {
                String payloadHash = bytesToHex(sha256(payload));
                builder.header("x-amz-content-sha256", payloadHash);

                // Cabecera de autorización simplificada para S3 / MinIO
                String canonicalRequest = method + "\n/" + bucket + "/" + key + "\n\n"
                        + "host:" + URI.create(endpoint).getHost() + "\n"
                        + "x-amz-date:" + amzDate + "\n\n"
                        + "host;x-amz-date\n"
                        + payloadHash;

                String stringToSign = "AWS4-HMAC-SHA256\n"
                        + amzDate + "\n"
                        + dateOnly + "/" + region + "/s3/aws4_request\n"
                        + bytesToHex(sha256(canonicalRequest.getBytes(StandardCharsets.UTF_8)));

                byte[] signingKey = getSignatureKey(secretKey, dateOnly, region, "s3");
                String signature = bytesToHex(hmacSha256(signingKey, stringToSign));

                String authHeader = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + dateOnly + "/" + region + "/s3/aws4_request, "
                        + "SignedHeaders=host;x-amz-date, Signature=" + signature;

                builder.header("Authorization", authHeader);
            } catch (Exception e) {
                log.warn("[Storage] Error generando firma SigV4: {}", e.getMessage());
            }
        }
    }

    private static byte[] sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, dateStamp);
        byte[] kRegion = hmacSha256(kDate, regionName);
        byte[] kService = hmacSha256(kRegion, serviceName);
        return hmacSha256(kService, "aws4_request");
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
