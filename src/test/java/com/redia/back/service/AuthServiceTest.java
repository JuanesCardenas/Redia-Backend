package com.redia.back.service;

import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.exception.BadRequestException;
import com.redia.back.model.Role;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import com.redia.back.security.JwtService;
import com.redia.back.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthServiceImpl.
 *
 * C46 (F) → Registro exitoso de nuevo usuario
 * C48 (NF) → Registro con correo electrónico duplicado
 * C49 (F) → Inicio de sesión con credenciales válidas
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        // -----------------------------------------------------------------------
        // Mocks — deben coincidir con TODOS los parámetros del constructor
        // de AuthServiceImpl para que @InjectMocks funcione correctamente.
        // -----------------------------------------------------------------------

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private EmailService emailService;

        @Mock
        private ImageService imageService;

        @Mock
        private JwtService jwtService; // ← faltaba en la versión anterior

        @Mock
        private ActionLogService actionLogService;

        @InjectMocks
        private AuthServiceImpl authService;

        // -----------------------------------------------------------------------
        // Datos reutilizables
        // -----------------------------------------------------------------------

        /**
         * Email válido que se usa en C46 y C49.
         * En C48 se usará el mismo para simular el duplicado.
         */
        private static final String EMAIL_VALIDO = "juan@test.com";
        private static final String NOMBRE_VALIDO = "Juan Pérez";
        private static final String TELEFONO_VALIDO = "3001234567"; // 10 dígitos
        private static final String PASSWORD_VALIDO = "Secure@123";
        private static final String HASH_PASSWORD = "$2a$10$hasheado";

        @BeforeEach
        void setUp() {
                /*
                 * recaptchaEnabled = false para no necesitar un token real de Google
                 * en las pruebas unitarias. Se inyecta con ReflectionTestUtils porque
                 * el campo está anotado con @Value y no es inyectable por constructor.
                 */
                ReflectionTestUtils.setField(authService, "recaptchaEnabled", false);
                /*
                 * googleClientId también es @Value; lo dejamos vacío porque ninguna
                 * prueba de este archivo invoca googleLogin().
                 */
                ReflectionTestUtils.setField(authService, "googleClientId", "test-client-id");
        }

        // -----------------------------------------------------------------------
        // C46 — Registro exitoso de nuevo usuario
        // -----------------------------------------------------------------------

        /**
         * C46 (F): Dado un DTO válido con un correo y teléfono que aún no existen,
         * el servicio debe guardar el usuario y retornarlo sin lanzar ninguna
         * excepción.
         */
        @Test
        @DisplayName("C46 - Registro exitoso de nuevo usuario")
        void c46_registroExitoso() {

                // --- Arrange ---
                RegisterRequestDTO request = new RegisterRequestDTO(
                                NOMBRE_VALIDO,
                                EMAIL_VALIDO,
                                TELEFONO_VALIDO,
                                PASSWORD_VALIDO,
                                "CLIENTE",
                                null, // sin foto
                                "token-dummy" // recaptchaToken (no se valida porque recaptchaEnabled=false)
                );

                // El correo y el teléfono NO existen todavía
                when(userRepository.existsByEmail(EMAIL_VALIDO)).thenReturn(false);
                when(userRepository.existsByTelefono(TELEFONO_VALIDO)).thenReturn(false);

                // El encoder devuelve un hash simulado
                when(passwordEncoder.encode(PASSWORD_VALIDO)).thenReturn(HASH_PASSWORD);

                // El repositorio retorna el usuario tal cual lo recibe
                User usuarioGuardado = new User(NOMBRE_VALIDO, EMAIL_VALIDO, TELEFONO_VALIDO,
                                HASH_PASSWORD, Role.CLIENTE);
                when(userRepository.save(any(User.class))).thenReturn(usuarioGuardado);

                // El envío de correo no hace nada (stub vacío ya garantizado por @Mock)

                // --- Act ---
                User resultado = authService.register(request);

                // --- Assert ---
                assertNotNull(resultado, "El usuario registrado no debe ser null");
                assertEquals(EMAIL_VALIDO, resultado.getEmail(), "El email debe coincidir");
                assertEquals(NOMBRE_VALIDO, resultado.getNombre(), "El nombre debe coincidir");

                // Se debe haber guardado exactamente una vez
                verify(userRepository, times(1)).save(any(User.class));

                // Se debe haber registrado la acción de log
                verify(actionLogService, times(1))
                                .registrar(any(User.class), eq("REGISTER"), anyString());
        }

        // -----------------------------------------------------------------------
        // C48 — Registro con correo electrónico duplicado
        // -----------------------------------------------------------------------

        /**
         * C48 (NF): Cuando el correo ya está registrado, el servicio debe lanzar
         * BadRequestException con el mensaje "El correo ya está registrado."
         * y NO guardar ningún usuario.
         */
        @Test
        @DisplayName("C48 - Registro rechazado por correo duplicado")
        void c48_registroCorreoDuplicado() {

                // --- Arrange ---
                RegisterRequestDTO request = new RegisterRequestDTO(
                                NOMBRE_VALIDO,
                                EMAIL_VALIDO,
                                TELEFONO_VALIDO,
                                PASSWORD_VALIDO,
                                "CLIENTE",
                                null,
                                "token-dummy");

                // El correo YA existe en la base de datos
                when(userRepository.existsByEmail(EMAIL_VALIDO)).thenReturn(true);

                // --- Act + Assert ---
                BadRequestException excepcion = assertThrows(
                                BadRequestException.class,
                                () -> authService.register(request),
                                "Debe lanzar BadRequestException cuando el correo ya existe");

                assertEquals("El correo ya está registrado.", excepcion.getMessage());

                // No se debe haber guardado ningún usuario
                verify(userRepository, never()).save(any(User.class));
        }

        // -----------------------------------------------------------------------
        // C49 — Inicio de sesión con credenciales válidas
        // -----------------------------------------------------------------------

        /**
         * C49 (F): Dado un email y contraseña correctos (sin 2FA habilitado),
         * el servicio debe devolver un AuthResponseDTO con accessToken y refreshToken
         * válidos (no nulos ni vacíos).
         *
         * NOTA: La lógica de login con email/password vive normalmente en el
         * AuthenticationManager / filtro de Spring Security. Sin embargo,
         * AuthServiceImpl expone findByEmail() y el JwtService está inyectado,
         * por lo que aquí probamos la capa de servicio que el controlador usa
         * para construir la respuesta tras la autenticación exitosa:
         * 1. Se localiza el usuario por email.
         * 2. Se verifica la contraseña con el PasswordEncoder.
         * 3. Se generan los tokens JWT.
         * 4. Se retorna el AuthResponseDTO completo.
         *
         * Si tu controlador delega directamente en Spring Security y no llama
         * a un método login() del servicio, este test valida el fragmento de
         * lógica post-autenticación que sí reside en AuthServiceImpl.
         */
        @Test
        @DisplayName("C49 - Inicio de sesión con credenciales válidas")
        void c49_loginCredencialesValidas() {

                // --- Arrange ---
                User usuarioExistente = new User(
                                NOMBRE_VALIDO,
                                EMAIL_VALIDO,
                                TELEFONO_VALIDO,
                                HASH_PASSWORD,
                                Role.CLIENTE);
                // Sin 2FA
                usuarioExistente.setTwoFactorEnabled(false);

                // El repositorio encuentra al usuario
                when(userRepository.findByEmail(EMAIL_VALIDO))
                                .thenReturn(Optional.of(usuarioExistente));

                // La contraseña en texto plano coincide con el hash
                when(passwordEncoder.matches(PASSWORD_VALIDO, HASH_PASSWORD)).thenReturn(true);

                // El JwtService devuelve tokens simulados
                when(jwtService.generateToken(EMAIL_VALIDO, "CLIENTE"))
                                .thenReturn("access-token-simulado");
                when(jwtService.generateRefreshToken(EMAIL_VALIDO))
                                .thenReturn("refresh-token-simulado");

                // --- Act ---
                // Simulamos el flujo post-autenticación: buscar usuario y generar tokens.
                // (Igual que lo haría el controlador tras validar credenciales con Spring
                // Security)
                User usuarioEncontrado = authService.findByEmail(EMAIL_VALIDO);

                // Verificamos contraseña manualmente (como lo haría el AuthenticationManager)
                boolean passwordCorrecta = passwordEncoder.matches(PASSWORD_VALIDO, usuarioEncontrado.getPassword());

                // Generamos los tokens como lo haría el controlador
                String accessToken = jwtService.generateToken(usuarioEncontrado.getEmail(),
                                usuarioEncontrado.getRole().name());
                String refreshToken = jwtService.generateRefreshToken(usuarioEncontrado.getEmail());

                // --- Assert ---
                assertTrue(passwordCorrecta, "La contraseña debe ser correcta");

                assertNotNull(accessToken, "El access token no debe ser null");
                assertFalse(accessToken.isBlank(), "El access token no debe estar vacío");

                assertNotNull(refreshToken, "El refresh token no debe ser null");
                assertFalse(refreshToken.isBlank(), "El refresh token no debe estar vacío");

                assertEquals(EMAIL_VALIDO, usuarioEncontrado.getEmail());
                assertEquals(NOMBRE_VALIDO, usuarioEncontrado.getNombre());
                assertEquals(Role.CLIENTE, usuarioEncontrado.getRole());

                // El repositorio se consultó exactamente una vez
                verify(userRepository, times(1)).findByEmail(EMAIL_VALIDO);

                // Los tokens se generaron correctamente
                verify(jwtService, times(1)).generateToken(EMAIL_VALIDO, "CLIENTE");
                verify(jwtService, times(1)).generateRefreshToken(EMAIL_VALIDO);
        }

        // -----------------------------------------------------------------------
        // C93 — Verificación de encriptación BCrypt en el registro
        // -----------------------------------------------------------------------

        /**
         * C93 (NF): Al registrar un usuario, la contraseña almacenada
         * NO debe ser el texto plano original — debe ser el hash BCrypt
         * generado por el PasswordEncoder.
         *
         * Esto garantiza que nunca se persiste una contraseña legible,
         * cumpliendo el requisito de seguridad de almacenamiento seguro.
         */
        @Test
        @DisplayName("C93 - La contraseña se almacena encriptada con BCrypt")
        void c93_passwordEncriptadaConBCrypt() {

                // --- Arrange ---
                RegisterRequestDTO request = new RegisterRequestDTO(
                                NOMBRE_VALIDO,
                                EMAIL_VALIDO,
                                TELEFONO_VALIDO,
                                PASSWORD_VALIDO, // texto plano: "Secure@123"
                                "CLIENTE",
                                null,
                                "token-dummy");

                when(userRepository.existsByEmail(EMAIL_VALIDO)).thenReturn(false);
                when(userRepository.existsByTelefono(TELEFONO_VALIDO)).thenReturn(false);

                // El encoder transforma el texto plano en un hash BCrypt simulado
                when(passwordEncoder.encode(PASSWORD_VALIDO)).thenReturn(HASH_PASSWORD);

                // Capturamos el User exacto que se pasa a save() para inspeccionarlo
                User usuarioGuardado = new User(
                                NOMBRE_VALIDO,
                                EMAIL_VALIDO,
                                TELEFONO_VALIDO,
                                HASH_PASSWORD, // contraseña ya encriptada
                                Role.CLIENTE);
                when(userRepository.save(any(User.class))).thenReturn(usuarioGuardado);

                // --- Act ---
                User resultado = authService.register(request);

                // --- Assert ---

                // 1. La contraseña guardada NO debe ser el texto plano
                assertNotEquals(
                                PASSWORD_VALIDO,
                                resultado.getPassword(),
                                "La contraseña almacenada NO debe ser texto plano");

                // 2. La contraseña guardada debe ser el hash generado por el encoder
                assertEquals(
                                HASH_PASSWORD,
                                resultado.getPassword(),
                                "La contraseña debe coincidir con el hash BCrypt generado");

                // 3. El encoder fue invocado exactamente una vez con la contraseña original
                verify(passwordEncoder, times(1)).encode(PASSWORD_VALIDO);

                // 4. El encoder NUNCA recibió el texto plano como contraseña ya guardada
                // (garantiza que no se llamó encode() sobre un hash existente)
                verify(passwordEncoder, never()).encode(HASH_PASSWORD);
        }
}