package com.redia.back.exception;

/**
 * Excepción lanzada cuando no se le permite el acceso al usuario.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}