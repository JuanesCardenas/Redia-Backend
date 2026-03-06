package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO utilizado para devolver información de reservas al cliente.
 */
public class ReservationResponseDTO {

    private String id;
    private String clienteEmail;
    private LocalDateTime fechaReserva;
    private int numeroPersonas;
    private String estado;

    public ReservationResponseDTO(String id, String clienteEmail, LocalDateTime fechaReserva, int numeroPersonas, String estado) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.fechaReserva = fechaReserva;
        this.numeroPersonas = numeroPersonas;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public int getNumeroPersonas() {
        return numeroPersonas;
    }

    public String getEstado() {
        return estado;
    }
}