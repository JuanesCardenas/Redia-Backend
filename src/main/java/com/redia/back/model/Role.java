package com.redia.back.model;

/**
 * Enum que define los roles disponibles en el sistema del restaurante.
 * Se usa para controlar permisos y accesos.
 */
public enum Role {
    CLIENTE,
    ADMINISTRADOR,
    RECEPCIONISTA,
    MESERO,
    COCINERO,
    AUXILIAR_COCINA,
    CAJERO
}