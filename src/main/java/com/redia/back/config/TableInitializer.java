package com.redia.back.config;

import com.redia.back.model.DinningTable;
import com.redia.back.repository.DinningTableRepository;
import com.redia.back.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Inicializa las 10 mesas fijas del restaurante al arrancar la aplicación.
 *
 * Comportamiento (completamente idempotente):
 *  - Si las 10 mesas correctas (IDs "1"-"10") ya existen en la BD →
 *    NO se toca nada: ni reservas ni mesas.
 *  - Si las mesas están incorrectas o faltan (p. ej. migración de esquema) →
 *    se eliminan TODAS las reservas con DELETE directo (sin cargar entidades,
 *    seguro aunque haya estados obsoletos como SOLICITADA/RECHAZADA en la BD)
 *    y luego se recrean las 10 mesas correctas.
 *
 * Mesas 1, 2, 9, 10  → capacidad 2 personas
 * Mesas 3, 4, 5, 6, 7, 8 → capacidad 4 personas
 */
@Configuration
public class TableInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TableInitializer.class);

    /** IDs exactos que deben existir en la BD. */
    private static final Set<String> EXPECTED_IDS =
            Set.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

    @Bean
    @Order(2) // Corre después del AdminInitializer (Order 1)
    public CommandLineRunner initTables(
            DinningTableRepository dinningTableRepository,
            ReservationRepository reservationRepository,
            JdbcTemplate jdbcTemplate) {

        return args -> {

            // Corrección en BD por cambio de esquema:
            // Intentar eliminar la columna categoria_id si aún existe en dishes,
            // ya que causa error 500 al insertar nuevos platillos usando ddl-auto=update.
            try {
                jdbcTemplate.execute("ALTER TABLE dishes DROP COLUMN categoria_id");
                logger.info("Columna 'categoria_id' eliminada exitosamente de la tabla dishes.");
            } catch (Exception e) {
                // Si la columna no existe o ya fue eliminada, fallará aquí (comportamiento esperado).
                logger.info("La columna 'categoria_id' no existía en dishes o ya había sido eliminada.");
            }

            // 1. Verificar si las 10 mesas correctas ya están en la BD.
            List<DinningTable> mesasActuales = dinningTableRepository.findAll();
            Set<String> idsActuales = new HashSet<>();
            for (DinningTable m : mesasActuales) {
                idsActuales.add(m.getId());
            }

            if (idsActuales.equals(EXPECTED_IDS)) {
                // Las mesas están bien → no se toca nada (ni reservas).
                logger.info("Las 10 mesas ya están correctamente inicializadas. No se realizan cambios.");
                return;
            }

            // 2. Las mesas están incorrectas o faltan → escenario de migración.
            //    Primero limpiar reservas con DELETE directo (sin cargar entidades
            //    en memoria) para evitar errores por estados obsoletos en el enum.
            logger.warn("Mesas incorrectas o faltantes (encontradas: {}). Iniciando migración...", idsActuales.size());
            logger.info("Eliminando reservas existentes (DELETE directo, seguro con enums obsoletos)...");
            reservationRepository.deleteAllReservations();
            logger.info("Reservas eliminadas.");

            // 3. Eliminar las mesas incorrectas y recrear las 10 correctas.
            dinningTableRepository.deleteAll();

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
            logger.info("Migración completada: 10 mesas inicializadas correctamente con estado DISPONIBLE.");
        };
    }
}
