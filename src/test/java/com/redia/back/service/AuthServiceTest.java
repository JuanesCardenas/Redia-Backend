package com.redia.back.service;

import com.redia.back.service.impl.AuthServiceImpl;
import com.redia.back.dto.RegisterRequestDTO;
import com.redia.back.model.User;
import com.redia.back.exception.BadRequestException;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.ActionLogService;
import com.redia.back.service.EmailService;
import com.redia.back.service.ImageService;
import com.redia.back.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas para AuthServiceImpl
 * 
 * Aquí se prueban los casos:
 * C46 → Registro exitoso
 * C48 → Registro con correo duplicado
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageService imageService;

    @Mock
    private EmailService emailService;

    @Mock
    private ActionLogService actionLogService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "recaptchaEnabled", false);
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan",
                "juan@test.com",
                "123456",
                "password",
                "CLIENTE",
                null,
                "token");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByTelefono(request.telefono())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        User savedUser = new User();
        when(userRepository.save(any())).thenReturn(savedUser);

        User result = authService.register(request);

        assertNotNull(result);
        verify(userRepository).save(any());
    }

    /**
     * C48: Registro con correo duplicado
     */
    @Test
    void register_emailDuplicado() {

        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO(
                "Juan",
                "juan@test.com",
                "123456789",
                "1234",
                "CLIENT",
                null,
                "token");

        // Simular que el correo ya existe
        when(userRepository.existsByEmail("juan@test.com")).thenReturn(true);

        // Act + Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> authService.register(request));

        // Validar mensaje de error
        assertEquals("El correo ya está registrado.", exception.getMessage());

        // Verificar que NO se guarda usuario
        verify(userRepository, never()).save(any());
    }
}