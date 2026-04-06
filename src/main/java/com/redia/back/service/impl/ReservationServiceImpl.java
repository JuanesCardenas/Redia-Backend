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
import com.redia.back.service.EmailService;
import com.redia.back.service.ReservationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de reservas.
 * Gestiona el ciclo completo de las reservas incluyendo notificaciones por
 * correo.
 */
@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DinningTableRepository dinningTableRepository;
    private final EmailService emailService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            DinningTableRepository dinningTableRepository,
            EmailService emailService) {

        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.dinningTableRepository = dinningTableRepository;
        this.emailService = emailService;
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

        // Enviar notificación de reserva solicitada
        String cuerpoReservaSolicitada = "Estimado/a " + cliente.getNombre() + ",\n\n" +
                "¡Bienvenido a Redia Restaurante!\n\n" +
                "Tu solicitud de reserva ha sido recibida exitosamente.\n\n" +
                "Detalles de tu solicitud:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Fecha y Hora: " + fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n"
                +
                "Número de Personas: " + numeroPersonas + "\n" +
                "Estado: SOLICITADA\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Tu reserva está en espera de confirmación por parte de nuestro equipo.\n" +
                "Recibirás una notificación cuando sea confirmada.\n\n" +
                "Si tienes alguna pregunta, no dudes en contactarnos.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "Tu reserva ha sido solicitada - Redia Restaurante",
                    cuerpoReservaSolicitada,
                    cliente.getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de reserva solicitada: {}", e.getMessage());
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

        // Enviar notificación de reserva confirmada
        String mesasInfo = reserva.getMesas().stream()
                .map(DinningTable::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No especificadas");

        String cuerpoReservaConfirmada = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "¡Excelente noticias! Tu reserva ha sido confirmada.\n\n" +
                "Detalles de tu reserva:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Fecha y Hora: "
                + reserva.getFechaReserva().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "\n" +
                "Número de Personas: " + reserva.getNumeroPersonas() + "\n" +
                "Número de Mesas: " + reserva.getNumeroMesas() + "\n" +
                "Mesas: " + mesasInfo + "\n" +
                "Estado: CONFIRMADA\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Tu mesa estará lista a la hora indicada. Te recomendamos llegar 10 minutos antes.\n\n" +
                "¡Te esperamos en Redia Restaurante!\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "¡Tu reserva ha sido confirmada! - Redia Restaurante",
                    cuerpoReservaConfirmada,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de reserva confirmada: {}", e.getMessage());
        }
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
                        r.getCliente().getNombre(),
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

        // Enviar notificación de confirmación
        String mesasInfo = reserva.getMesas().stream()
                .map(DinningTable::getNombre)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No especificadas");

        String cuerpoConfirmacion = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "¡Excelente noticias! Tu reserva ha sido confirmada.\n\n" +
                "Detalles de tu reserva:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Fecha y Hora: "
                + reserva.getFechaReserva().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + "\n" +
                "Número de Personas: " + reserva.getNumeroPersonas() + "\n" +
                "Mesas: " + mesasInfo + "\n" +
                "Estado: CONFIRMADA\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Tu mesa estará lista a la hora indicada.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "🎉 ¡Tu reserva ha sido confirmada! - Redia Restaurante",
                    cuerpoConfirmacion,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de confirmación: {}", e.getMessage());
        }
    }

    @Override
    public void rechazarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.RECHAZADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} rechazada", reservaId);

        // Enviar notificación de rechazo
        String cuerpoRechazo = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "Lamentablemente, tu reserva ha sido rechazada.\n\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Estado: RECHAZADA\n\n" +
                "Esto puede deberse a la indisponibilidad de mesas en el horario solicitado.\n" +
                "Te invitamos a intentar con otra fecha u hora.\n\n" +
                "Para más información, puedes contactarnos directamente.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "Tu reserva ha sido rechazada - Redia Restaurante",
                    cuerpoRechazo,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de rechazo: {}", e.getMessage());
        }
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

        // Enviar notificación de cancelación
        String cuerpoCancelacion = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "Tu reserva ha sido cancelada.\n\n" +
                "ID de Reserva: " + reserva.getId() + "\n" +
                "Estado: CANCELADA\n\n" +
                "Si tienes alguna pregunta sobre esta cancelación, por favor contacta con nuestro equipo.\n\n" +
                "Esperamos tu próxima visita a Redia Restaurante.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "Tu reserva ha sido cancelada - Redia Restaurante",
                    cuerpoCancelacion,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de cancelación: {}", e.getMessage());
        }
    }

    @Override
    public void finalizarReserva(String reservaId) {

        Reservation reserva = reservationRepository.findById(reservaId)
                .orElseThrow(() -> new BadRequestException("Reserva no encontrada"));

        reserva.setEstado(ReservationStatus.FINALIZADA);

        reservationRepository.save(reserva);

        logger.info("Reserva {} finalizada", reservaId);

        // Enviar notificación de finalización
        String cuerpoFinalizacion = "Estimado/a " + reserva.getCliente().getNombre() + ",\n\n" +
                "¡Gracias por tu visita a Redia Restaurante!\n\n" +
                "Esperamos que hayas disfrutado de una excelente experiencia con nosotros.\n" +
                "Tu satisfacción es nuestra prioridad.\n\n" +
                "Te invitamos a visitarnos nuevamente pronto.\n\n" +
                "Saludos cordiales,\n" +
                "Equipo de Redia Restaurante";

        try {
            emailService.sendMail(new com.redia.back.dto.EmailDTO(
                    "Gracias por tu visita - Redia Restaurante",
                    cuerpoFinalizacion,
                    reserva.getCliente().getEmail()));
        } catch (Exception e) {
            logger.error("Error enviando correo de finalización: {}", e.getMessage());
        }
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
     * - Domingo: no permitido
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
    }

    /**
     * Tarea programada (Cron/FixedRate) para finalizar automáticamente
     * las reservas confirmadas cuya hora de fin ya ha pasado.
     */
    @Scheduled(fixedRate = 60000) // Se ejecuta cada 60 segundos
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

    @Override
    public List<com.redia.back.dto.TableAvailabilityDTO> getMesasDisponibles(LocalDateTime inicio, LocalDateTime fin) {
        // Obtenemos todas las mesas
        List<DinningTable> todasMesas = dinningTableRepository.findAll();
        // Obtenemos aquellas que SÍ están disponibles
        List<DinningTable> mesasDisponibles = dinningTableRepository.findMesasDisponibles(inicio, fin);
        
        Set<String> idDisponibles = mesasDisponibles.stream()
                .map(DinningTable::getId)
                .collect(Collectors.toSet());

        return todasMesas.stream().map(mesa -> new com.redia.back.dto.TableAvailabilityDTO(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getCapacidad(),
                idDisponibles.contains(mesa.getId())
        )).collect(Collectors.toList());
    }
}
