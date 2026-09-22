package com.tecmilenio.mapsconect.exception;

/**
 * Excepcion de conflicto (409): el recurso ya existe o hay un conflicto de estado.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String message) {
        super(message);
    }
}
