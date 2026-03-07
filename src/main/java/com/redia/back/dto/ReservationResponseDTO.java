package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO utilizado para devolver información de reservas al cliente.
 */
public record ReservationResponseDTO(

        String id,
        String clienteEmail,
        LocalDateTime fechaReserva,
        int numeroPersonas,
        String estado

) {
}