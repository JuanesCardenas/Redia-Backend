package com.redia.back.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador exclusivo para usuarios con rol ADMINISTRADOR.
 */
@RestController
public class AdminController {

    /**
     * Endpoint accesible solo por ADMINISTRADOR.
     * Usa anotación @PreAuthorize para validar el rol.
     */
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/api/admin/dashboard")
    public String dashboard() {
        return "Bienvenido al panel de administrador 🔥";
    }
}