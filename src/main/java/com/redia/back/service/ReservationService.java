package com.redia.back.service;

import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.dto.TableAvailabilityDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que define la lógica de negocio de reservas.
 */
public interface ReservationService {

    /**
     * Crear una nueva reserva (queda CONFIRMADA automáticamente con las mesas elegidas).
     */
    ReservationResponseDTO crearReserva(CreateReservationRequestDTO request);

    /**
     * Obtener reservas del cliente autenticado.
     */
    List<ReservationResponseDTO> obtenerReservasCliente();

    /**
     * Obtener todas las reservas (recepcionista/admin).
     */
    List<ReservationResponseDTO> obtenerTodas();

    /**
     * Cancelar una reserva (cliente — requiere 24 h de anticipación).
     */
    void cancelarReserva(String reservaId);

    /**
     * Cancelar una reserva sin restricción de tiempo (uso exclusivo del recepcionista).
     */
    void cancelarReservaForzado(String reservaId);

    /**
     * Finalizar reserva cuando el cliente ya fue atendido.
     */
    void finalizarReserva(String reservaId);

    /**
     * Obtener el listado de todas las mesas y si están disponibles para un rango horario.
     */
    List<TableAvailabilityDTO> getMesasDisponibles(LocalDateTime inicio, LocalDateTime fin);
}