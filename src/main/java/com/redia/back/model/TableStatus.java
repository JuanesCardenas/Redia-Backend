package com.redia.back.model;

/**
 * Enum que representa los posibles estados de una mesa en el restaurante.
 */
public enum TableStatus {

    // La mesa está disponible para reservas
    DISPONIBLE,

    // La mesa está ocupada (hay una reserva activa)
    OCUPADA,

    // La mesa está fuera de servicio temporalmente
    FUERA_DE_SERVICIO
}
