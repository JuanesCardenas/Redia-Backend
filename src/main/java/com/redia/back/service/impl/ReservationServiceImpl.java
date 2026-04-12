package com.redia.back.service.impl;

import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.dto.TableAvailabilityDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.DinningTable;
import com.redia.back.model.Reservation;
import com.redia.back.model.ReservationStatus;
import com.redia.back.model.User;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.ReservationRepository;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.EmailService;
import com.redia.back.service.ReservationService;
import com.redia.back.service.ActionLogService;

import com.redia.back.dto.EmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final EmailService emailService;
    private final ActionLogService actionLogService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            DinningTableRepository dinningTableRepository,
            EmailService emailService,
            ActionLogService actionLogService) {

        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.dinningTableRepository = dinningTableRepository;
        this.emailService = emailService;
        this.actionLogService = actionLogService;
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
     * Crear una nueva reserva. El cliente selecciona las mesas y la reserva
     * queda CONFIRMADA automáticamente.
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
        if (numeroPersonas <= 0) {
            throw new BadRequestException("El número de personas debe ser mayor a 0.");
        }

        // Obtener y validar las mesas seleccionadas por el cliente
        java.util.List<String> tableIds = request.tableIds();
        if (tableIds == null || tableIds.isEmpty()) {
            throw new BadRequestException("Debes seleccionar al menos una mesa.");
        }

        List<DinningTable> mesasSeleccionadas = dinningTableRepository.findAllById(tableIds);
        if (mesasSeleccionadas.isEmpty()) {
            throw new BadRequestException("No se encontraron las mesas seleccionadas en el sistema.");
        }

        // Verificar disponibilidad en el horario solicitado
        List<DinningTable> mesasDisponibles = dinningTableRepository.findMesasDisponibles(fecha, horaFinReserva);
        Set<String> idsDisponibles = mesasDisponibles.stream()
                .map(DinningTable::getId)
                .collect(Collectors.toSet());

        for (DinningTable mesa : mesasSeleccionadas) {
            if (!idsDisponibles.contains(mesa.getId())) {
                throw new BadRequestException(
                        "La mesa " + mesa.getNombre() + " no está disponible en ese horario.");
            }
        }

        // Validar que la capacidad cubre el número de personas
        int capacidadMesas = mesasSeleccionadas.stream().mapToInt(DinningTable::getCapacidad).sum();
        if (capacidadMesas < numeroPersonas) {
            throw new BadRequestException(
                    "Las mesas seleccionadas solo tienen capacidad para " + capacidadMesas
                            + " personas, pero indicaste " + numeroPersonas + ".");
        }

        // Crear la reserva directamente CONFIRMADA
        Reservation reserva = new Reservation(
                cliente,
                fecha,
                horaFinReserva,
                numeroPersonas,
                mesasSeleccionadas,
                ReservationStatus.CONFIRMADA);

        reservationRepository.save(reserva);

        actionLogService.registrar(cliente, "CREAR_RESERVA", "Reserva creada con ID: " + reserva.getId());

        logger.info("Reserva {} creada y confirmada automáticamente", reserva.getId());

        // Notificar al cliente
        String mesasInfo = mesasSeleccionadas.stream()
                .map(DinningTable::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No especificadas");

        String cuerpo = "Estimado/a " + cliente.getNombre() + ",\n\n" +
                "¡Tu reserva ha sido confirmada exitosamente!\n\n" +
                "Detalles de tu reserva:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Fecha y Hora: " + fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n"
                +
                "Número de Personas: " + numeroPersonas + "\n" +
                "Mesas: " + mesasInfo + "\n" +
                "Estado: CONFIRMADA\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Tu mesa estará lista a la hora indicada. Te recomendamos llegar 10 minutos antes.\n\n" +
                "¡Te esperamos en Redia Restaurante!\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new EmailDTO(
                    "¡Tu reserva está confirmada! - Redia Restaurante",
                    cuerpo,
                    cliente.getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de reserva confirmada: {}", e.getMessage());
        }

        return new ReservationResponseDTO(
                reserva.getId(),
                cliente.getEmail(),
                cliente.getNombre(),
                reserva.getFechaReserva(),
                reserva.getHoraFinReserva(),
                reserva.getNumeroPersonas(),
                reserva.getEstado().name());
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
                        cliente.getNombre(),
                        r.getFechaReserva(),
                        r.getHoraFinReserva(),
                        r.getNumeroPersonas(),
                        r.getEstado().name()))
                .collect(Collectors.toList());
    }

    /**
     * Obtener todas las reservas (uso administrativo/recepcionista).
     */
    @Override
    public List<ReservationResponseDTO> obtenerTodas() {
        logger.info("Consulta de todas las reservas del sistema");

        return reservationRepository.findAll()
                .stream()
                .map(r -> new ReservationResponseDTO(
                        r.getId(),
                        r.getCliente().getEmail(),
                        r.getCliente().getNombre(),
                        r.getFechaReserva(),
                        r.getHoraFinReserva(),
                        r.getNumeroPersonas(),
                        r.getEstado().name()))
                .collect(Collectors.toList());
    }

    /**
     * Cancelar una reserva (cliente — requiere 24 h de anticipación).
     */
    @Override
    public void cancelarReserva(String reservaId) {
        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        long horas = Duration.between(LocalDateTime.now(), reserva.getFechaReserva()).toHours();
        if (horas < 24) {
            logger.warn("Intento de cancelación fuera del tiempo permitido para reserva {}", reservaId);
            throw new BadRequestException("No se puede cancelar con menos de 24 horas de anticipación.");
        }

        cancelarYNotificar(reserva);
        User usuario = obtenerUsuarioAutenticado();

        actionLogService.registrar(usuario, "CANCELAR_RESERVA", "Cancelación de reserva ID: " + reservaId);
    }

    /**
     * Cancelar una reserva sin restricción de tiempo (uso exclusivo del
     * recepcionista).
     */
    @Override
    public void cancelarReservaForzado(String reservaId) {
        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        if (reserva.getEstado() == ReservationStatus.CANCELADA
                || reserva.getEstado() == ReservationStatus.FINALIZADA) {
            throw new BadRequestException("La reserva ya está " + reserva.getEstado().name().toLowerCase() + ".");
        }

        User usuario = obtenerUsuarioAutenticado();
        actionLogService.registrar(usuario, "CANCELAR_RESERVA_FORZADA",
                "Cancelación forzada de reserva ID: " + reservaId);

        logger.info("Recepcionista cancela forzosamente la reserva {}", reservaId);
        cancelarYNotificar(reserva);
    }

    /** Lógica compartida de cancelación: cambia estado y envía correo. */
    private void cancelarYNotificar(Reservation reserva) {
        reserva.setEstado(ReservationStatus.CANCELADA);
        reservationRepository.save(reserva);
        logger.info("Reserva {} cancelada", reserva.getId());

        String cuerpo = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "Tu reserva ha sido cancelada.\n\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Estado: CANCELADA\n\n" +
                "Si tienes alguna pregunta, por favor contacta con nuestro equipo.\n\n" +
                "Esperamos tu próxima visita a Redia Restaurante.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new EmailDTO(
                    "Tu reserva ha sido cancelada - Redia Restaurante",
                    cuerpo,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de cancelación: {}", e.getMessage());
        }
    }

    /**
     * Finalizar una reserva.
     */
    @Override
    public void finalizarReserva(String reservaId) {
        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.FINALIZADA);
        reservationRepository.save(reserva);

        User usuario = obtenerUsuarioAutenticado();
        actionLogService.registrar(usuario, "FINALIZAR_RESERVA", "Reserva finalizada con ID: " + reservaId);

        logger.info("Reserva {} finalizada", reservaId);

        String cuerpo = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "¡Gracias por tu visita a Redia Restaurante!\n\n" +
                "Esperamos que hayas disfrutado de una excelente experiencia con nosotros.\n" +
                "Tu satisfacción es nuestra prioridad.\n\n" +
                "Te invitamos a visitarnos nuevamente pronto.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new EmailDTO(
                    "Gracias por tu visita - Redia Restaurante",
                    cuerpo,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de finalización: {}", e.getMessage());
        }
    }

    /**
     * Tarea programada para finalizar automáticamente las reservas confirmadas
     * cuya hora de fin ya ha pasado.
     */
    @Scheduled(fixedRate = 60000)
    public void finalizarReservasExpiradas() {
        List<Reservation> expiradas = reservationRepository
                .findByEstadoAndHoraFinReservaBefore(ReservationStatus.CONFIRMADA, LocalDateTime.now());

        if (!expiradas.isEmpty()) {
            logger.info("Finalizando automáticamente {} reservas expiradas...", expiradas.size());
            for (Reservation r : expiradas) {
                r.setEstado(ReservationStatus.FINALIZADA);
            }
            reservationRepository.saveAll(expiradas);
            logger.info("Reservas finalizadas con éxito.");
        }
    }

    /**
     * Obtener disponibilidad de todas las mesas para un rango horario.
     */
    @Override
    public List<TableAvailabilityDTO> getMesasDisponibles(LocalDateTime inicio, LocalDateTime fin) {
        List<DinningTable> todasMesas = dinningTableRepository.findAll();
        List<DinningTable> mesasDisponibles = dinningTableRepository.findMesasDisponibles(inicio, fin);

        Set<String> idsDisponibles = mesasDisponibles.stream()
                .map(DinningTable::getId)
                .collect(Collectors.toSet());

        return todasMesas.stream().map(mesa -> new TableAvailabilityDTO(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getCapacidad(),
                idsDisponibles.contains(mesa.getId()),
                mesa.getEstado().name())).collect(Collectors.toList());
    }

    // ===================================================
    // Métodos privados de validación
    // ===================================================

    private void validarDuracionReserva(LocalDateTime inicio, LocalDateTime fin) {

        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new BadRequestException("La reserva debe comenzar y terminar el mismo día.");
        }

        long horas = java.time.Duration.between(inicio, fin).toHours();

        if (horas <= 0) {
            throw new BadRequestException("La hora de finalización debe ser posterior al inicio.");
        }

        if (horas > 3) {
            throw new BadRequestException("Una reserva no puede durar más de 3 horas.");
        }
    }

    /**
     * Valida que la reserva esté dentro del horario permitido:
     * - Lunes a viernes: 8:00 - 22:00
     * - Sábado: 8:00 - 16:00
     * - Domingo: no permitido
     */
    private void validarHorarioReserva(LocalDateTime inicio, LocalDateTime fin) {

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

        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new BadRequestException("La reserva debe iniciar y terminar el mismo día.");
        }
    }
}
