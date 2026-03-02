package com.redia.back.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de prueba para verificar control de roles.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String soloAdmin() {
        return "Acceso permitido solo para ADMINISTRADOR";
    }

    @GetMapping("/mesero")
    @PreAuthorize("hasRole('MESERO')")
    public String soloMesero() {
        return "Acceso permitido solo para MESERO";
    }
}