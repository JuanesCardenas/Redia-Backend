package com.redia.back.service;

import com.redia.back.dto.ConfirmReservationRequestDTO;
import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;

import java.util.List;

/**
 * Servicio que define la lógica de negocio de reservas.
 */
public interface ReservationService {

    /**
     * Crear una nueva reserva.
     */
    ReservationResponseDTO crearReserva(CreateReservationRequestDTO request);

    /**
     * Asigna mesas a una reserva y la confirma.
     */
    void assignTablesAndConfirmReservation(String reservationId, ConfirmReservationRequestDTO request);

    /**
     * Obtener reservas del cliente autenticado.
     */
    List<ReservationResponseDTO> obtenerReservasCliente();

    /**
     * Obtener todas las reservas (recepcionista/admin).
     */
    List<ReservationResponseDTO> obtenerTodas();

    /**
     * Confirmar una reserva.
     */
    void confirmarReserva(String reservaId);

    /**
     * Rechazar una reserva.
     */
    void rechazarReserva(String reservaId);

    /**
     * Cancelar una reserva.
     */
    void cancelarReserva(String reservaId);

    /**
     * Finalizar reserva cuando el cliente llega.
     */
    void finalizarReserva(String reservaId);

    /**
     * Obtener el listado de todas las mesas y si están disponibles.
     */
    List<com.redia.back.dto.TableAvailabilityDTO> getMesasDisponibles(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);
}