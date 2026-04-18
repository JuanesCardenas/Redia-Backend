package com.redia.back.controller;

import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.dto.UserResponseDTO;
import com.redia.back.model.User;
import com.redia.back.service.AuthService;
import com.redia.back.service.UserAdminService;

import com.redia.back.exception.BadRequestException;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la administración de usuarios.
 * Solo accesible para usuarios con rol ADMIN.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final AuthService authService;
    private final UserAdminService userAdminService;
    private final com.redia.back.service.RecaptchaService recaptchaService;

    @Value("${google.recaptcha.enabled:false}")
    private boolean recaptchaEnabled;

    public AdminUserController(AuthService authService,
            UserAdminService userAdminService,
            com.redia.back.service.RecaptchaService recaptchaService) {
        this.authService = authService;
        this.userAdminService = userAdminService;
        this.recaptchaService = recaptchaService;
    }

    /**
     * Permite al administrador registrar un nuevo usuario.
     * Utiliza el mismo flujo de registro que el endpoint público.
     */
    @PostMapping
    public ResponseEntity<String> registerUser(

            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String telefono,
            @RequestParam String role,
            @RequestParam String recaptchaToken,
            @RequestParam(required = false) MultipartFile fotoUrl) {

        logger.info("ADMIN registrando usuario con email: {}", email);

        if (recaptchaEnabled && !recaptchaService.validateRecaptcha(recaptchaToken)) {
            throw new BadRequestException("Validación de reCAPTCHA fallida.");
        }

        RegisterRequestDTO request = new RegisterRequestDTO(nombre, email, telefono, password, role, fotoUrl,
                recaptchaToken);

        authService.register(request);

        logger.info("Usuario registrado por ADMIN: {}", email);

        return ResponseEntity.ok("Usuario registrado correctamente.");
    }

    /**
     * Obtiene todos los usuarios del sistema.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        logger.info("ADMIN solicitó la lista de usuarios");

    /**
     * Elimina un usuario por su id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) {

        logger.info("ADMIN eliminando usuario con id: {}", id);

        userAdminService.deleteUser(id);

        logger.info("Usuario eliminado: {}", id);

        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }
}