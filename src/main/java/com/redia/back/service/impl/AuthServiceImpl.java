package com.redia.back.service.impl;

import com.redia.back.dto.*;
import com.redia.back.exception.BadRequestException;
import com.redia.back.exception.MissingCredentialsException;
import com.redia.back.model.Role;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import com.redia.back.service.AuthService;
import com.redia.back.service.EmailService;
import com.redia.back.service.ImageService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Implementación del servicio de autenticación.
 * Gestiona registro, recuperación de contraseña y cambios de contraseña con
 * integración de email e imágenes.
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ImageService imageService;
    private final com.redia.back.security.JwtService jwtService;

    @Value("${google.client-id}")
    private String googleClientId;

    public AuthServiceImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            ImageService imageService,
            com.redia.back.security.JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.imageService = imageService;
        this.jwtService = jwtService;
    }

    @Override
    public User register(RegisterRequestDTO request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El correo ya está registrado.");
        }

        if (userRepository.existsByTelefono(request.telefono())) {
            throw new BadRequestException("El teléfono ya está registrado.");
        }

        Role role;

        try {
            role = Role.valueOf(request.role().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Rol inválido.");
        }

        if (request.email() == null || request.email().isEmpty()) {
            throw new MissingCredentialsException("El correo es requerido.");
        }

        if (request.password() == null || request.password().isEmpty()) {
            throw new MissingCredentialsException("La contraseña es requerida.");
        }

        if (request.nombre() == null || request.nombre().isEmpty()) {
            throw new MissingCredentialsException("El nombre es requerido.");
        }

        if (request.telefono() == null || request.telefono().isEmpty()) {
            throw new MissingCredentialsException("El teléfono es requerido.");
        }

        User user = new User(
                request.nombre(),
                request.email(),
                request.telefono(),
                passwordEncoder.encode(request.password()),
                role);

        // Subir la imagen a Cloudinary si se proporciona
        if (request.fotoUrl() != null && !request.fotoUrl().isEmpty()) {
            try {
                Map<String, Object> uploadResult = imageService.upload(request.fotoUrl());
                String fotoUrl = uploadResult.get("url").toString();
                user.setFotoUrl(fotoUrl);
            } catch (Exception e) {
                throw new BadRequestException("Error al subir la imagen: " + e.getMessage());
            }
        }

        User userGuardado = userRepository.save(user);

        // Enviar correo de bienvenida
        try {
            emailService.sendMail(
                    new EmailDTO(
                            "¡Bienvenido a Redia!",
                            "Hola " + user.getNombre() + ",\n\n" +
                                    "¡Bienvenido a Redia! Tu registro ha sido completado exitosamente.\n\n" +
                                    "Ya puedes acceder a la plataforma con tu correo y contraseña.\n\n" +
                                    "Saludos,\nEl equipo de Redia",
                            user.getEmail()));
        } catch (Exception e) {
            // Log del error pero no interrumpir el flujo de registro
            System.err.println("Error al enviar correo de bienvenida: " + e.getMessage());
        }

        return userGuardado;
    }

    @Override
    public void recoverPassword(PasswordRecoveryRequestDTO request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

        user.setPassword(passwordEncoder.encode(request.nuevaPassword()));

        userRepository.save(user);

        // Enviar correo de confirmación
        try {
            emailService.sendMail(
                    new EmailDTO(
                            "Tu contraseña se ha actualizado correctamente",
                            "Hola " + user.getNombre() + ",\n\n" +
                                    "Realizaste un cambio de contraseña en tu cuenta de Redia.\n" +
                                    "Si no fuiste tú, por favor contáctanos de inmediato.\n\n" +
                                    "Saludos,\nEl equipo de Redia",
                            user.getEmail()));
        } catch (Exception e) {
            System.err.println("Error al enviar correo de confirmación: " + e.getMessage());
        }
    }

    @Override
    public void sendVerificationCode(ForgotPasswordDTO forgotPasswordDTO) {

        User usuario = userRepository.findByEmail(forgotPasswordDTO.email())
                .orElseThrow(() -> new BadRequestException("El usuario no existe."));

        // Generar un código aleatorio de 6 dígitos
        String codigo = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Guardar el código y su fecha de expiración
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiraEn(LocalDateTime.now().plusMinutes(10));
        userRepository.save(usuario);

        // Enviar el correo con el código
        try {
            emailService.sendMail(
                    new EmailDTO(
                            "Recuperación de contraseña",
                            "Hola " + usuario.getNombre() + ",\n\n" +
                                    "Recibimos una solicitud para recuperar tu contraseña.\n" +
                                    "Tu código de verificación es: " + codigo + "\n" +
                                    "Este código expira en 10 minutos.\n\n" +
                                    "Si no solicitaste esto, ignora este correo.\n\n" +
                                    "Saludos,\nEl equipo de Redia",
                            usuario.getEmail()));
        } catch (Exception e) {
            throw new BadRequestException("Error al enviar el código de verificación.");
        }
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {

        User usuario = userRepository.findByEmail(resetPasswordDTO.email())
                .orElseThrow(() -> new BadRequestException("El usuario no existe."));

        // Se verifica que el código coincide
        if (usuario.getCodigoVerificacion() == null ||
                !usuario.getCodigoVerificacion().equals(resetPasswordDTO.verificationCode())) {
            throw new BadRequestException("El código de verificación es incorrecto.");
        }

        // Se verifica que el código no haya expirado
        if (usuario.getCodigoExpiraEn() == null || usuario.getCodigoExpiraEn().isBefore(LocalDateTime.now())) {
            usuario.setCodigoVerificacion(null);
            usuario.setCodigoExpiraEn(null);
            userRepository.save(usuario);
            throw new BadRequestException("El código de verificación ha expirado.");
        }

        // Actualizamos la contraseña
        usuario.setPassword(passwordEncoder.encode(resetPasswordDTO.newPassword()));

        // Se limpia el código usado
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiraEn(null);

        userRepository.save(usuario);

        // Enviar correo de confirmación
        try {
            emailService.sendMail(
                    new EmailDTO(
                            "Tu contraseña se ha actualizado correctamente",
                            "Hola " + usuario.getNombre() + ",\n\n" +
                                    "Tu contraseña ha sido reseteada exitosamente.\n" +
                                    "Ya puedes iniciar sesión con tu nueva contraseña.\n\n" +
                                    "Saludos,\nEl equipo de Redia",
                            usuario.getEmail()));
        } catch (Exception e) {
            System.err.println("Error al enviar correo de confirmación: " + e.getMessage());
        }
    }

    @Override
    public void changePassword(String userId, ChangePasswordDTO changePasswordDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));

        // Verificar que la contraseña actual es correcta
        if (!passwordEncoder.matches(changePasswordDTO.oldPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta.");
        }

        // Verificar que la nueva contraseña no sea igual a la actual
        if (changePasswordDTO.oldPassword().equals(changePasswordDTO.newPassword())) {
            throw new BadRequestException("La nueva contraseña no puede ser igual a la actual.");
        }

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(changePasswordDTO.newPassword()));
        userRepository.save(user);

        // Enviar correo de confirmación
        try {
            emailService.sendMail(
                    new EmailDTO(
                            "Tu contraseña se ha actualizado correctamente",
                            "Hola " + user.getNombre() + ",\n\n" +
                                    "Tu contraseña ha sido cambiada exitosamente.\n" +
                                    "Si no fuiste tú, por favor contáctanos de inmediato.\n\n" +
                                    "Saludos,\nEl equipo de Redia",
                            user.getEmail()));
        } catch (Exception e) {
            System.err.println("Error al enviar correo de confirmación: " + e.getMessage());
        }
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado."));
    }
}
