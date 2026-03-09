package com.redia.back.controller;

import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.dto.UserResponseDTO;
import com.redia.back.model.User;
import com.redia.back.service.AuthService;
import com.redia.back.service.UserAdminService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AdminUserController(AuthService authService,
            UserAdminService userAdminService) {
        this.authService = authService;
        this.userAdminService = userAdminService;
    }

    /**
     * Permite al administrador registrar un nuevo usuario.
     * Utiliza el mismo flujo de registro que el endpoint público.
     */
    @PostMapping
    public ResponseEntity<String> registerUser(

            @Valid @RequestParam String nombre,
            @Valid @RequestParam String email,
            @Valid @RequestParam String password,
            @Valid @RequestParam String telefono,
            @Valid @RequestParam String role,
            @RequestParam(required = false) MultipartFile fotoUrl) {

        logger.info("ADMIN registrando usuario con email: {}", email);

        RegisterRequestDTO request = new RegisterRequestDTO(nombre, email, telefono, password, role, fotoUrl, null);

        authService.registerAdmin(request);

        logger.info("Usuario registrado por ADMIN: {}", email);

        return ResponseEntity.ok("Usuario registrado correctamente.");
    }

    /**
     * Obtiene todos los usuarios del sistema.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        logger.info("ADMIN solicitó la lista de usuarios");

        return ResponseEntity.ok(userAdminService.getAllUsers());
    }

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