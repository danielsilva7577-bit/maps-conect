package com.tecmilenio.mapsconect.exception;

/**
 * Se lanza cuando un usuario/IP excede el límite de rate limiting
 * en un endpoint protegido (mensajes, foro, uploads, etc.).
 */
public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(String message) {
        super(message);
        this.retryAfterSeconds = 60; // default: reintentar en 60 s
    }

    public TooManyRequestsException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * @return segundos recomendados hasta el próximo reintento (para el header HTTP Retry-After).
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
