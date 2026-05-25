package com.redia.back.service;

import com.redia.back.dto.CreateReservationRequestDTO;
import com.redia.back.dto.EmailDTO;
import com.redia.back.dto.ReservationResponseDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.DinningTable;
import com.redia.back.model.Reservation;
import com.redia.back.model.ReservationStatus;
import com.redia.back.model.Role;
import com.redia.back.model.User;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.ReservationRepository;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.impl.ReservationServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReservationServiceImpl.
 *
 * C56 (F) → Creación exitosa de reserva con datos válidos
 * C57 (NF) → Prevención de duplicidad de mesa y horario
 * C80 (F) → Envío de notificación tras confirmación de reserva
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DinningTableRepository dinningTableRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private ActionLogService actionLogService;

    // Mocks de Spring Security para simular usuario autenticado
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    // -----------------------------------------------------------------------
    // Datos reutilizables
    // -----------------------------------------------------------------------

    private static final String EMAIL_CLIENTE = "cliente@test.com";
    private static final String NOMBRE_CLIENTE = "Ana García";
    private static final String MESA_ID = "3";
    private static final String MESA_NOMBRE = "Mesa 3";
    private static final int MESA_CAPACIDAD = 4;

    /**
     * Fecha válida: mañana a las 12:00 (lunes a viernes, dentro del horario).
     * Se calcula en cada test para que nunca quede en el pasado.
     */
    private LocalDateTime fechaValida;
    private LocalDateTime horaFinValida;

    private User clienteMock;
    private DinningTable mesaMock;

    @BeforeEach
    void setUp() {
        // Fecha futura garantizada: próximo lunes o el día actual si es laborable
        fechaValida = proximoLaborable().withHour(12).withMinute(0).withSecond(0).withNano(0);
        horaFinValida = fechaValida.withHour(14); // 2 horas después (máx 3 h)

        // Usuario autenticado simulado
        clienteMock = new User(NOMBRE_CLIENTE, EMAIL_CLIENTE, "3009876543", "hash", Role.CLIENTE);

        // Mesa simulada con capacidad suficiente
        mesaMock = new DinningTable(MESA_ID, MESA_NOMBRE, MESA_CAPACIDAD);

        // Configurar SecurityContextHolder para que obtenerUsuarioAutenticado()
        // funcione
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL_CLIENTE);
        when(userRepository.findByEmail(EMAIL_CLIENTE)).thenReturn(Optional.of(clienteMock));
    }

    // -----------------------------------------------------------------------
    // C56 — Creación exitosa de reserva con datos válidos
    // -----------------------------------------------------------------------

    /**
     * C56 (F): Dado un request con fecha futura, mesas disponibles y capacidad
     * suficiente, el servicio debe crear la reserva con estado CONFIRMADA
     * y retornar un ReservationResponseDTO completo y no nulo.
     */
    @Test
    @DisplayName("C56 - Creacion exitosa de reserva con datos validos")
    void c56_creacionExitosaDeReserva() {

        // --- Arrange ---
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(
                fechaValida,
                horaFinValida,
                2, // 2 personas — la mesa tiene capacidad para 4
                List.of(MESA_ID));

        // El repositorio encuentra la mesa seleccionada
        when(dinningTableRepository.findAllById(List.of(MESA_ID)))
                .thenReturn(List.of(mesaMock));

        // La mesa está disponible en ese horario
        when(dinningTableRepository.findMesasDisponibles(fechaValida, horaFinValida))
                .thenReturn(List.of(mesaMock));

        // El repositorio guarda y devuelve la reserva
        Reservation reservaGuardada = new Reservation(
                clienteMock,
                fechaValida,
                horaFinValida,
                2,
                List.of(mesaMock),
                ReservationStatus.CONFIRMADA);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservaGuardada);

        // --- Act ---
        ReservationResponseDTO resultado = reservationService.crearReserva(request);

        // --- Assert ---
        assertNotNull(resultado, "El DTO de respuesta no debe ser null");
        assertEquals(EMAIL_CLIENTE, resultado.clienteEmail(), "El email del cliente debe coincidir");
        assertEquals(NOMBRE_CLIENTE, resultado.clienteNombre(), "El nombre del cliente debe coincidir");
        assertEquals(2, resultado.numeroPersonas(), "El número de personas debe coincidir");
        assertEquals(ReservationStatus.CONFIRMADA.name(), resultado.estado(), "La reserva debe quedar CONFIRMADA");

        // Se debe haber guardado exactamente una reserva
        verify(reservationRepository, times(1)).save(any(Reservation.class));

        // Se debe haber registrado la acción en el log
        verify(actionLogService, times(1))
                .registrar(any(User.class), eq("CREAR_RESERVA"), anyString());
    }

    // -----------------------------------------------------------------------
    // C57 — Prevención de duplicidad de mesa y horario
    // -----------------------------------------------------------------------

    /**
     * C57 (NF): Cuando una mesa ya tiene una reserva en el horario solicitado,
     * el servicio debe lanzar BadRequestException indicando que la mesa no está
     * disponible, y NO debe persistir ninguna reserva nueva.
     */
    @Test
    @DisplayName("C57 - Prevencion de duplicidad de mesa y horario")
    void c57_prevencionDuplicidadMesaYHorario() {

        // --- Arrange ---
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(
                fechaValida,
                horaFinValida,
                2,
                List.of(MESA_ID));

        // La mesa existe en el sistema
        when(dinningTableRepository.findAllById(List.of(MESA_ID)))
                .thenReturn(List.of(mesaMock));

        // La mesa NO aparece en la lista de disponibles (ya tiene reserva en ese
        // horario)
        when(dinningTableRepository.findMesasDisponibles(fechaValida, horaFinValida))
                .thenReturn(List.of()); // lista vacía = ninguna mesa disponible

        // --- Act + Assert ---
        BadRequestException excepcion = assertThrows(
                BadRequestException.class,
                () -> reservationService.crearReserva(request),
                "Debe lanzar BadRequestException cuando la mesa ya está ocupada en ese horario");

        // El mensaje debe mencionar el nombre de la mesa no disponible
        assertTrue(
                excepcion.getMessage().contains(MESA_NOMBRE),
                "El error debe mencionar qué mesa no está disponible. Mensaje: " + excepcion.getMessage());

        // No se debe haber guardado ninguna reserva
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // -----------------------------------------------------------------------
    // C80 — Envío de notificación tras confirmación de reserva
    // -----------------------------------------------------------------------

    /**
     * C80 (F): Al confirmar una reserva exitosamente, el servicio debe invocar
     * emailService.sendMail() exactamente una vez con el correo del cliente,
     * garantizando que la notificación de confirmación se despacha.
     * 
     * @throws Exception
     */
    @Test
    @DisplayName("C80 - Envio de notificacion tras confirmacion de reserva")
    void c80_envioNotificacionTrasConfirmacion() throws Exception {

        // --- Arrange ---
        CreateReservationRequestDTO request = new CreateReservationRequestDTO(
                fechaValida,
                horaFinValida,
                2,
                List.of(MESA_ID));

        when(dinningTableRepository.findAllById(List.of(MESA_ID)))
                .thenReturn(List.of(mesaMock));

        when(dinningTableRepository.findMesasDisponibles(fechaValida, horaFinValida))
                .thenReturn(List.of(mesaMock));

        Reservation reservaGuardada = new Reservation(
                clienteMock,
                fechaValida,
                horaFinValida,
                2,
                List.of(mesaMock),
                ReservationStatus.CONFIRMADA);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservaGuardada);

        // --- Act ---
        reservationService.crearReserva(request);

        // --- Assert ---

        // Capturamos el EmailDTO para verificar destinatario y que se llamó una vez
        org.mockito.ArgumentCaptor<EmailDTO> captor = org.mockito.ArgumentCaptor.forClass(EmailDTO.class);

        verify(emailService, times(1)).sendMail(captor.capture());

        EmailDTO emailEnviado = captor.getValue();
        assertEquals(
                EMAIL_CLIENTE,
                emailEnviado.recipient(), // ← corregido: recipient() no destinatario()
                "El correo de confirmacion debe enviarse al cliente que hizo la reserva");
    }

    // -----------------------------------------------------------------------
    // Método auxiliar
    // -----------------------------------------------------------------------

    /**
     * Devuelve el próximo día laborable (lunes a viernes) a partir de mañana,
     * para garantizar que los tests nunca usen una fecha pasada o fin de semana.
     */
    private LocalDateTime proximoLaborable() {
        LocalDateTime candidato = LocalDateTime.now().plusDays(1);
        while (candidato.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || candidato.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            candidato = candidato.plusDays(1);
        }
        return candidato;
    }
}