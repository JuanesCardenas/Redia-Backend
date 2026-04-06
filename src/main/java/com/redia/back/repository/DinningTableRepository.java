package com.redia.back.repository;

import com.redia.back.model.DinningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DinningTableRepository extends JpaRepository<DinningTable, String> {

    void deleteById(String id);

    /**
     * Obtiene mesas disponibles en un rango de tiempo.
     */
    @Query("""
                SELECT t FROM DinningTable t
                WHERE t.id NOT IN (
                    SELECT m.id FROM Reservation r
                    JOIN r.mesas m
                    WHERE r.fechaReserva < :horaFin
                    AND r.horaFinReserva > :horaInicio
                    AND r.estado NOT IN (com.redia.back.model.ReservationStatus.CANCELADA, com.redia.back.model.ReservationStatus.RECHAZADA)
                )
            """)
    List<DinningTable> findMesasDisponibles(LocalDateTime horaInicio, LocalDateTime horaFin);
}