package com.redia.back.model;

/**
 * Enum que representa los posibles estados de una reserva
 * dentro del sistema.
 */
public enum ReservationStatus {

    // La reserva fue confirmada automáticamente al crearla
    CONFIRMADA,

    // El cliente o el recepcionista canceló la reserva
    CANCELADA,

    // La reserva ya se realizó en el restaurante
    FINALIZADA
}