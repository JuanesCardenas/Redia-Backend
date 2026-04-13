package com.redia.back.repository;

import com.redia.back.model.Reservation;
import com.redia.back.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para acceder a las reservas en la base de datos.
 */
public interface ReservationRepository extends JpaRepository<Reservation, String> {

    /**
     * Obtiene todas las reservas de un cliente.
     *
     * @param userId id del cliente
     * @return lista de reservas
     */
    List<Reservation> findByClienteId(String userId);

    /**
     * Busca reservas en una fecha específica.
     */
    List<Reservation> findByFechaReserva(LocalDateTime fechaReserva);

    /**
     * Busca reservas por estado.
     */
    List<Reservation> findByEstado(ReservationStatus estado);

    /**
     * Busca reservas por estado cuya hora de fin ya haya pasado.
     */
    List<Reservation> findByEstadoAndHoraFinReservaBefore(ReservationStatus estado, LocalDateTime hora);

    /**
     * Busca reservas que se solapan con un rango de tiempo.
     */
    @Query("""
                SELECT r FROM Reservation r
                WHERE r.fechaReserva < :horaFin
                AND r.horaFinReserva > :horaInicio
            """)
    List<Reservation> findReservasSolapadas(LocalDateTime horaInicio, LocalDateTime horaFin);

    /**
     * Elimina TODAS las reservas directamente con SQL (sin cargarlas en memoria).
     * Necesario cuando la BD tiene estados obsoletos que el enum ya no reconoce.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r")
    void deleteAllReservations();
}