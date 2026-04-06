package com.redia.back.dto;

import java.time.LocalDateTime;

/**
 * DTO que representa la solicitud de creación de una reserva.
 */
public record CreateReservationRequestDTO(

                /**
                 * Fecha y hora solicitada para la reserva
                 */
                LocalDateTime fechaReserva,

                /**
                 * Fecha y hora de fin para la reserva
                 */
                LocalDateTime horaFinReserva,

                /**
                 * Número de personas
                 */
                int numeroPersonas,

                /**
                 * IDs de las mesas seleccionadas
                 */
                java.util.List<String> tableIds

) {
}