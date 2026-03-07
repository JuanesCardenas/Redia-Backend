package com.redia.back.exception;

/**
 * Excepción lanzada cuando faltan credenciales.
 */
public class MissingCredentialsException extends RuntimeException {

    public MissingCredentialsException(String message) {
        super(message);
    }
}
