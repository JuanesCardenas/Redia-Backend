package com.redia.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal que arranca la aplicación Spring Boot.
 */
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class RediaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RediaBackendApplication.class, args);
    }

}