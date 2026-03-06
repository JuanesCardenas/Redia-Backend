package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa la solicitud de creación de una reserva.
 */
public class CreateReservationRequestDTO {

    /**
     * Fecha y hora solicitada para la reserva
     */
    private LocalDateTime fechaReserva;

    /**
     * Número de personas
     */
    private int numeroPersonas;

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public void setNumeroPersonas(int numeroPersonas) {
        this.numeroPersonas = numeroPersonas;
    }
}