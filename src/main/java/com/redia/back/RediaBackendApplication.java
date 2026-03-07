package com.redia.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal que arranca la aplicación Spring Boot.
 */
@EnableAsync
@SpringBootApplication
public class RediaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RediaBackendApplication.class, args);
    }

}