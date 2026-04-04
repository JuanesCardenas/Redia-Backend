package com.redia.back.config;

import com.redia.back.model.Role;
import com.redia.back.model.User;
import com.redia.back.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class AdminInitializer {

    @Value("${ADMIN_EMAIL:admin@redia.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:Admin123}")
    private String adminPassword;

    @Bean
    CommandLineRunner crearAdmin(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = new User();
                admin.setNombre("Administrador");
                admin.setEmail(adminEmail);
                admin.setTelefono("3000000000");
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMINISTRADOR);
                admin.setActivo(true);

                userRepository.save(admin);

                System.out.println("ADMINISTRADOR CREADO: " + adminEmail);
            }
        };
    }
}