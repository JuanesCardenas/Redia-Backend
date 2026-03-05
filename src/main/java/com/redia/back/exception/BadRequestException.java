package com.redia.back.exception;

/**
 * Excepción lanzada cuando el cliente envía datos inválidos.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}