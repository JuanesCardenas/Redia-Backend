package com.redia.back.model;

/**
 * Enum que representa los posibles estados de una reserva
 * dentro del sistema.
 */
public enum ReservationStatus {

    // El cliente solicitó la reserva
    SOLICITADA,

    // La reserva fue validada y confirmada
    CONFIRMADA,

    // No había disponibilidad
    RECHAZADA,

    // El cliente canceló la reserva
    CANCELADA,

    // La reserva ya se realizó en el restaurante
    FINALIZADA
}