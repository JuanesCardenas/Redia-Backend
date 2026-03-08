package com.redia.back.service.impl;

import com.redia.back.dto.ConfirmReservationRequestDTO;
import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.DinningTable;
import com.redia.back.model.Reservation;
import com.redia.back.model.ReservationStatus;
import com.redia.back.model.User;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.ReservationRepository;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de reservas.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DinningTableRepository dinningTableRepository;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            DinningTableRepository dinningTableRepository) {

        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.dinningTableRepository = dinningTableRepository;
    }

    /**
     * Obtiene el usuario autenticado desde el JWT.
     */
    private User obtenerUsuarioAutenticado() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));
    }

    /**
     * Crear una nueva reserva.
     */
    @Override
    public ReservationResponseDTO crearReserva(CreateReservationRequestDTO request) {

        User cliente = obtenerUsuarioAutenticado();

        logger.info("Solicitud de reserva por usuario: {}", cliente.getEmail());

        LocalDateTime fecha = request.fechaReserva();
        LocalDateTime horaFinReserva = request.horaFinReserva();

        if (fecha.isBefore(LocalDateTime.now())) {
            logger.warn("Intento de reserva en fecha pasada por usuario {}", cliente.getEmail());
            throw new BadRequestException("No se puede reservar en una fecha pasada.");
        }

        validarHorarioReserva(fecha, horaFinReserva);

        validarDuracionReserva(fecha, horaFinReserva);

        int numeroPersonas = request.numeroPersonas();

        // ===========================
        // Validar máximo de personas
        // ===========================
        if (numeroPersonas <= 0) {
            throw new BadRequestException("El número de personas debe ser mayor a 0.");
        }

        List<DinningTable> todasMesas = dinningTableRepository.findAll();
        int capacidadTotal = todasMesas.stream().mapToInt(DinningTable::getCapacidad).sum();

        if (numeroPersonas > capacidadTotal) {
            logger.warn("Intento de reserva con {} personas, excede capacidad total {}", numeroPersonas,
                    capacidadTotal);
            throw new BadRequestException(
                    "El número de personas excede la capacidad máxima del restaurante: " + capacidadTotal);
        }

        Reservation reserva = new Reservation(
                cliente,
                fecha,
                horaFinReserva,
                numeroPersonas,
                new ArrayList<>() // SIN mesas todavía
        );

        reserva.setEstado(ReservationStatus.SOLICITADA);

        reservationRepository.save(reserva);

        logger.info("Reserva solicitada con id {}", reserva.getId());

        return new ReservationResponseDTO(
                reserva.getId(),
                cliente.getEmail(),
                reserva.getFechaReserva(),
                reserva.getHoraFinReserva(),
                reserva.getNumeroPersonas(),
                reserva.getEstado().name());
    }

    @Override
    public void assignTablesAndConfirmReservation(String reservaId, ConfirmReservationRequestDTO request) {

        logger.info("Recepcionista intenta confirmar reserva {}", reservaId);

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        if (reserva.getEstado() != ReservationStatus.SOLICITADA) {
            throw new BadRequestException("Solo se pueden confirmar reservas en estado SOLICITADA.");
        }

        LocalDateTime inicio = reserva.getFechaReserva();
        LocalDateTime fin = reserva.getHoraFinReserva();

        List<String> mesasIds = request.mesasIds();

        List<DinningTable> mesasSeleccionadas = dinningTableRepository.findAllById(mesasIds);

        if (mesasSeleccionadas.isEmpty()) {
            throw new BadRequestException("Debe seleccionar al menos una mesa.");
        }

        /**
         * Verificar disponibilidad
         */
        List<DinningTable> mesasDisponibles = dinningTableRepository.findMesasDisponibles(inicio, fin);

        for (DinningTable mesa : mesasSeleccionadas) {

            boolean disponible = mesasDisponibles.stream()
                    .anyMatch(m -> m.getId().equals(mesa.getId()));

            if (!disponible) {

                logger.warn("Mesa {} no está disponible", mesa.getId());

                throw new BadRequestException(
                        "La mesa " + mesa.getNombre() + " no está disponible en ese horario.");
            }
        }

        /**
         * Validar capacidad
         */
        int capacidadTotal = mesasSeleccionadas.stream()
                .mapToInt(DinningTable::getCapacidad)
                .sum();

        if (capacidadTotal < reserva.getNumeroPersonas()) {

            throw new BadRequestException(
                    "Las mesas seleccionadas no tienen capacidad suficiente para "
                            + reserva.getNumeroPersonas() + " personas.");
        }

        /**
         * Asignar mesas y confirmar
         */
        reserva.setMesas(mesasSeleccionadas);
        reserva.setNumeroMesas(mesasSeleccionadas.size());
        reserva.setEstado(ReservationStatus.CONFIRMADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} confirmada con {} mesas",
                reservaId, mesasSeleccionadas.size());
    }

    /**
     * Obtener reservas del cliente autenticado.
     */
    @Override
    public List<ReservationResponseDTO> obtenerReservasCliente() {

        User cliente = obtenerUsuarioAutenticado();

        logger.info("Consultando reservas del cliente {}", cliente.getEmail());

        return reservationRepository.findByClienteId(cliente.getId())
                .stream()
                .map(r -> new ReservationResponseDTO(
                        r.getId(),
                        cliente.getEmail(),
                        r.getFechaReserva(),
                        r.getHoraFinReserva(),
                        r.getNumeroPersonas(),
                        r.getEstado().name()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las reservas (uso administrativo).
     */
    @Override
    public List<ReservationResponseDTO> obtenerTodas() {

        logger.info("Consulta de todas las reservas del sistema");

        return reservationRepository.findAll()
                .stream()
                .map(r -> new ReservationResponseDTO(
                        r.getId(),
                        r.getCliente().getEmail(),
                        r.getFechaReserva(),
                        r.getHoraFinReserva(),
                        r.getNumeroPersonas(),
                        r.getEstado().name()))
                .collect(Collectors.toList());
    }

    @Override
    public void confirmarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.CONFIRMADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} confirmada", reservaId);
    }

    @Override
    public void rechazarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.RECHAZADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} rechazada", reservaId);
    }

    @Override
    public void cancelarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        long horas = Duration.between(LocalDateTime.now(), reserva.getFechaReserva()).toHours();

        if (horas < 24) {
            logger.warn("Intento de cancelación fuera del tiempo permitido para reserva {}", reservaId);
            throw new BadRequestException("No se puede cancelar con menos de 24 horas.");
        }

        reserva.setEstado(ReservationStatus.CANCELADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} cancelada", reservaId);
    }

    @Override
    public void finalizarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.FINALIZADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} finalizada", reservaId);
    }

    private void validarDuracionReserva(LocalDateTime inicio, LocalDateTime fin) {

        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new IllegalArgumentException("La reserva debe comenzar y terminar el mismo día.");
        }

        long horas = java.time.Duration.between(inicio, fin).toHours();

        if (horas <= 0) {
            throw new IllegalArgumentException("La hora de finalización debe ser posterior al inicio.");
        }

        if (horas > 3) {
            throw new IllegalArgumentException("Una reserva no puede durar más de 3 horas.");
        }
    }

    /**
     * Valida que la reserva esté dentro del horario permitido:
     * - Lunes a viernes: 8:00 - 22:00
     * - Sábado: 8:00 - 16:00
     * - Domingo y festivos: no permitido
     */
    private void validarHorarioReserva(LocalDateTime inicio, LocalDateTime fin) {

        // Verificar que sea día permitido
        switch (inicio.getDayOfWeek()) {
            case SUNDAY -> throw new BadRequestException("No se permiten reservas los domingos.");
            case SATURDAY -> {
                if (inicio.getHour() < 8 || fin.getHour() > 16) {
                    throw new BadRequestException("El horario de sábado es de 8:00 a 16:00.");
                }
            }
            default -> { // Lunes a viernes
                if (inicio.getHour() < 8 || fin.getHour() > 22) {
                    throw new BadRequestException("El horario de lunes a viernes es de 8:00 a 22:00.");
                }
            }
        }

        // Validar que la fecha de inicio y fin estén en el mismo día
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new BadRequestException("La reserva debe iniciar y terminar el mismo día.");
        }

        // Validar festivos en Colombia
        if (esFestivoColombia(inicio.toLocalDate())) {
            throw new BadRequestException("No se permiten reservas en festivos.");
        }
    }

    /**
     * Verifica si una fecha es festivo en Colombia (lista estática).
     */
    private boolean esFestivoColombia(LocalDate fecha) {
        Set<LocalDate> festivos = Set.of(
                // 2026
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 2),
                LocalDate.of(2026, 4, 3),
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 6, 8),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 8, 7),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 12, 8),
                LocalDate.of(2026, 12, 25));

        return festivos.contains(fecha);
    }
}