package com.redia.back.config;

import com.redia.back.model.DinningTable;
import com.redia.back.model.TableStatus;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Inicializa las 10 mesas fijas del restaurante al arrancar la aplicación.
 * También elimina todas las reservas y mesas existentes para que la BD
 * quede sincronizada con el mapa del frontend.
 *
 * Mesas 1, 2, 9, 10  → capacidad 2 personas
 * Mesas 3, 4, 5, 6, 7, 8 → capacidad 4 personas
 */
@Configuration
public class TableInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TableInitializer.class);

    @Bean
    @Order(2) // Corre después del AdminInitializer (Order 1)
    public CommandLineRunner initTables(
            DinningTableRepository dinningTableRepository,
            ReservationRepository reservationRepository) {

        return args -> {
            // 1. Eliminar TODAS las reservas primero (para liberar la FK hacia mesas)
            long totalReservas = reservationRepository.count();
            if (totalReservas > 0) {
                logger.info("Eliminando {} reservas existentes...", totalReservas);
                reservationRepository.deleteAll();
                logger.info("Reservas eliminadas.");
            }

            // 2. Eliminar todas las mesas existentes
            long totalMesas = dinningTableRepository.count();
            if (totalMesas > 0) {
                logger.info("Eliminando {} mesas existentes para re-inicializar...", totalMesas);
                dinningTableRepository.deleteAll();
            }

            // 3. Insertar las 10 mesas fijas con IDs "1"-"10" (igual que en el frontend)
            logger.info("Inicializando las 10 mesas fijas del restaurante...");

            List<DinningTable> tables = List.of(
                    new DinningTable("1",  "1",  2),  // Mesa 01 — 2 personas
                    new DinningTable("2",  "2",  2),  // Mesa 02 — 2 personas
                    new DinningTable("3",  "3",  4),  // Mesa 03 — 4 personas
                    new DinningTable("4",  "4",  4),  // Mesa 04 — 4 personas
                    new DinningTable("5",  "5",  4),  // Mesa 05 — 4 personas
                    new DinningTable("6",  "6",  4),  // Mesa 06 — 4 personas
                    new DinningTable("7",  "7",  4),  // Mesa 07 — 4 personas
                    new DinningTable("8",  "8",  4),  // Mesa 08 — 4 personas
                    new DinningTable("9",  "9",  2),  // Mesa 09 — 2 personas
                    new DinningTable("10", "10", 2)   // Mesa 10 — 2 personas
            );

            dinningTableRepository.saveAll(tables);
            logger.info("10 mesas inicializadas correctamente con estado DISPONIBLE.");
        };
    }
}
