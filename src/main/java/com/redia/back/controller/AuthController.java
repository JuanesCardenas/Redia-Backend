package com.redia.back.controller;

import com.redia.back.dto.*;
import com.redia.back.exception.MissingCredentialsException;
import com.redia.back.model.User;
import com.redia.back.security.JwtService;
import com.redia.back.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    public AuthController(AuthService authService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String telefono,
            @RequestParam String role,
            @RequestParam(required = false) MultipartFile fotoUrl) {

        logger.info("Intento de registro para email: {}", email);

        if (nombre == null || nombre.isEmpty()) {
            throw new MissingCredentialsException("El nombre es requerido.");
        }

        if (email == null || email.isEmpty()) {
            throw new MissingCredentialsException("El correo es requerido.");
        }

        if (password == null || password.isEmpty()) {
            throw new MissingCredentialsException("La contraseña es requerida.");
        }

        if (telefono == null || telefono.isEmpty()) {
            throw new MissingCredentialsException("El teléfono es requerido.");
        }

        if (role == null || role.isEmpty()) {
            throw new MissingCredentialsException("El rol es requerido.");
        }

        RegisterRequestDTO request = new RegisterRequestDTO(nombre, email, password, telefono, role, fotoUrl);

        authService.register(request);

        logger.info("Registro exitoso para email: {}", email);

        return ResponseEntity.ok("Usuario registrado correctamente. Se envió un correo de bienvenida.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {

        logger.info("Intento de login para email: {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = authService.findByEmail(request.email());

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        logger.info("Login exitoso para email: {}", request.email());

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        accessToken,
                        refreshToken,
                        user.getEmail(),
                        user.getRole().name()));
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
                        user.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        logger.info("Solicitud de logout");

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendVerificationCode(@Valid @RequestBody ForgotPasswordDTO request) {

        logger.info("Solicitud de código de verificación para email: {}", request.email());

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
}