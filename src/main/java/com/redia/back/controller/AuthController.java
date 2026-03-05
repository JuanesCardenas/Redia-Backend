package com.redia.back.controller;

import com.redia.back.dto.*;
import com.redia.back.model.User;
import com.redia.back.security.JwtService;
import com.redia.back.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO request) {

        logger.info("Intento de registro para email: {}", request.getEmail());

        authService.register(request);

        logger.info("Registro exitoso para email: {}", request.getEmail());

        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {

        logger.info("Intento de login para email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = authService.findByEmail(request.getEmail());

        String accessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        logger.info("Login exitoso para email: {}", request.getEmail());

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        accessToken,
                        refreshToken,
                        user.getEmail(),
                        user.getRole().name()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody RefreshTokenRequestDTO request) {

        logger.info("Solicitud de refresh token");

        String email = jwtService.extractEmail(request.getRefreshToken());

        if (!jwtService.isRefreshTokenValid(request.getRefreshToken())) {
            logger.warn("Refresh token inválido");
            throw new RuntimeException("Refresh token inválido");
        }

        User user = authService.findByEmail(email);

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        logger.info("Nuevo access token generado para {}", email);

        return ResponseEntity.ok(
                new AuthResponseDTO(
                        newAccessToken,
                        request.getRefreshToken(),
                        user.getEmail(),
                        user.getRole().name()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {

        logger.info("Solicitud de logout");

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}