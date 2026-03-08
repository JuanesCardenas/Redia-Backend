package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO utilizado para devolver información de reservas al cliente.
 */
public record ReservationResponseDTO(

                String id,
                String clienteEmail,
                LocalDateTime fechaReserva,
                LocalDateTime horaFinReserva,
                int numeroPersonas,
                String estado

) {
}