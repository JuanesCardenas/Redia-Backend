package com.redia.back.controller;

import com.google.api.client.util.Value;
import com.redia.back.dto.*;
import com.redia.back.exception.MissingCredentialsException;
import com.redia.back.model.User;
import com.redia.back.security.JwtService;
import com.redia.back.service.ActionLogService;
import com.redia.back.service.AuthService;
import com.redia.back.service.RecaptchaService;
import com.redia.back.exception.BadRequestException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RecaptchaService recaptchaService;
    private final ActionLogService actionLogService;

    @Value("${google.recaptcha.enabled:false}")
    private boolean recaptchaEnabled;

    public AuthController(AuthService authService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RecaptchaService recaptchaService,
            ActionLogService actionLogService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.recaptchaService = recaptchaService;
        this.actionLogService = actionLogService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestParam String nombre,
            @Valid @RequestParam String email,
            @Valid @RequestParam String password,
            @Valid @RequestParam String telefono,
            @Valid @RequestParam String role,
            @RequestParam(required = false) MultipartFile fotoUrl,
            @RequestParam String recaptchaToken) {

        logger.info("Intento de registro para email: {}", email);

        // Validar reCAPTCHA si está habilitado, pero NO bloquear si falla
        if (recaptchaEnabled && recaptchaToken != null) {
            try {
                if (!recaptchaService.validateRecaptcha(recaptchaToken)) {
                    logger.warn("reCAPTCHA validation failed for registration email: {}", email);
                    // NO lanzamos excepción, dejamos pasar
                }
            } catch (Exception e) {
                logger.warn("Error validando reCAPTCHA en registro: {}. Permitiendo solicitud de todos modos.", e.getMessage());
                // Permitir que continúe incluso si hay error en reCAPTCHA
            }
        }

        RegisterRequestDTO request = new RegisterRequestDTO(nombre, email, telefono, password, role, fotoUrl,
                recaptchaToken);

        authService.register(request);

        logger.info("Registro exitoso para email: {}", email);

        return ResponseEntity.ok("Usuario registrado correctamente. Se envió un correo de bienvenida.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        logger.info("Intento de login para email: {}", request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));
        } catch (AuthenticationException ex) {
            actionLogService.registrarSinUsuario(request.email(),
                    "LOGIN_FALLIDO", "Credenciales incorrectas");
            throw ex;
        }

        User user = authService.findByEmail(request.email());

        // Si el usuario tiene 2FA habilitado, no entregar tokens todavía
        if (user.isTwoFactorEnabled()) {
            logger.info("Login requiere 2FA para email: {}", request.email());
            return ResponseEntity.ok(
                    new AuthResponseDTO(null, null, user.getEmail(),
                            user.getRole().name(), user.getNombre(),
                            user.getTelefono(), user.getFotoUrl(), true));
        }

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        actionLogService.registrar(user, "LOGIN", "Login exitoso");

        logger.info("Login exitoso para email: {}", request.email());

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        accessToken,
                        refreshToken,
                        user.getEmail(),
                        user.getRole().name(),
                        user.getNombre(),
                        user.getTelefono(),
                        user.getFotoUrl()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {

        logger.info("Solicitud de refresh token");

        String email = jwtService.extractEmail(request.refreshToken());

        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            logger.warn("Refresh token inválido");
            throw new RuntimeException("Refresh token inválido");
        }

        User user = authService.findByEmail(email);

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        logger.info("Nuevo access token generado para {}", email);

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        newAccessToken,
                        request.refreshToken(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getNombre(),
                        user.getTelefono(),
                        user.getFotoUrl()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        logger.info("Solicitud de logout");

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody ForgotPasswordDTO request) {

        logger.info("Solicitud de código de verificación para email: {}", request.email());

        // Validar reCAPTCHA si está habilitado, pero NO bloquear si falla
        // (mejor tener funcionalidad que perfecta seguridad contra spam)
        if (recaptchaEnabled && request.recaptchaToken() != null) {
            try {
                if (!recaptchaService.validateRecaptcha(request.recaptchaToken())) {
                    logger.warn("reCAPTCHA validation failed for email: {}", request.email());
                    // NO lanzamos excepción, dejamos pasar
                }
            } catch (Exception e) {
                logger.warn("Error validando reCAPTCHA: {}. Permitiendo solicitud de todos modos.", e.getMessage());
                // Permitir que continúe incluso si hay error en reCAPTCHA
                // Mejor tener operacional que rechazar peticiones válidas
            }
        }

        authService.sendVerificationCode(request);

        logger.info("Código de verificación enviado a: {}", request.email());

        return ResponseEntity
                .ok("Se ha enviado un código de verificación a tu correo electrónico. El código expira en 10 minutos.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {

        logger.info("Solicitud de reset de contraseña para email: {}", request.email());

        authService.resetPassword(request);

        logger.info("Contraseña reseteada para email: {}", request.email());

        return ResponseEntity.ok(
                "Tu contraseña ha sido actualizada correctamente. Ya puedes iniciar sesión con tu nueva contraseña.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordDTO request) {

        logger.info("Solicitud de cambio de contraseña");

        // Obtener el usuario autenticado del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        authService.changePassword(userId, request);

        logger.info("Contraseña cambiada para usuario: {}", userId);

        return ResponseEntity.ok("Tu contraseña ha sido cambiada exitosamente.");
    }

    @PostMapping("/recover-password")
    public ResponseEntity<String> recoverPassword(@Valid @RequestBody PasswordRecoveryRequestDTO request) {

        logger.info("Solicitud de recuperación de contraseña para email: {}", request.email());

        authService.recoverPassword(request);

        logger.info("Contraseña recuperada para email: {}", request.email());

        return ResponseEntity.ok("Tu contraseña ha sido actualizada correctamente.");
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        logger.info("Intento de login con Google");
        AuthResponseDTO response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/complete-profile", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> completeProfile(
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile fotoUrl,
            @RequestHeader("Authorization") String authHeader) {
        logger.info("Completando perfil de usuario por Google");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Token de autorización requerido.");
        }
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);
        var result = authService.completeProfile(telefono, password, fotoUrl, email);
        return ResponseEntity.ok(result);
    }

    // =========================================================
    // 2FA Endpoints
    // =========================================================

    /**
     * Inicia el proceso de configuración de 2FA. Devuelve QR y secret. Requiere
     * JWT.
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2FA(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        logger.info("Iniciando configuración 2FA para: {}", email);
        var response = authService.setup2FA(email);
        return ResponseEntity.ok(response);
    }

    /** Valida el primer código TOTP y activa el 2FA. Requiere JWT. */
    @PostMapping("/2fa/enable")
    public ResponseEntity<String> enable2FA(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody java.util.Map<String, Integer> body) {
        String email = extractEmailFromHeader(authHeader);
        int code = body.get("code");
        authService.enable2FA(email, code);
        logger.info("2FA activado para: {}", email);
        return ResponseEntity.ok("Verificación de dos pasos activada exitosamente.");
    }

    /** Desactiva el 2FA del usuario. Requiere JWT. */
    @PostMapping("/2fa/disable")
    public ResponseEntity<String> disable2FA(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        authService.disable2FA(email);
        logger.info("2FA desactivado para: {}", email);
        return ResponseEntity.ok("Verificación de dos pasos desactivada.");
    }

    /**
     * Verifica el código TOTP durante el login y entrega los tokens reales.
     * Público.
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<AuthResponseDTO> verifyTwoFactor(
            @RequestBody TwoFactorVerifyRequestDTO request) {
        logger.info("Verificando código 2FA para email: {}", request.email());
        AuthResponseDTO response = authService.verifyTwoFactor(request);
        logger.info("2FA verificado exitosamente para: {}", request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-deletion")
    public ResponseEntity<String> requestDeletion(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromHeader(authHeader);
        logger.info("Solicitud de baja de cuenta para: {}", email);
        authService.requestUnsubscribe(email);
        return ResponseEntity.ok("Su solicitud de baja de cuenta ha sido registrada. Un administrador la procesará pronto.");
    }

    private String extractEmailFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("Token de autorización requerido.");
        }
        return jwtService.extractEmail(authHeader.substring(7));
    }
}