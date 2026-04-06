package com.redia.back.config;

import com.redia.back.model.DinningTable;
import com.redia.back.repository.DinningTableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TableInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TableInitializer.class);

    @Bean
    public CommandLineRunner initTables(DinningTableRepository dinningTableRepository) {
        return args -> {
            if (dinningTableRepository.count() == 0) {
                logger.info("Initializing the 10 tables for the visual map...");

                List<DinningTable> tables = List.of(
                        new DinningTable("1", 2), // Mesa 01 - Small
                        new DinningTable("2", 2), // Mesa 02 - Small
                        new DinningTable("3", 4), // Mesa 03 - 4 chairs
                        new DinningTable("4", 4), // Mesa 04 - 4 chairs
                        new DinningTable("5", 4), // Mesa 05 - 4 chairs
                        new DinningTable("6", 4), // Mesa 06 - 4 chairs
                        new DinningTable("7", 4), // Mesa 07 - 4 chairs
                        new DinningTable("8", 4), // Mesa 08 - 4 chairs
                        new DinningTable("9", 2), // Mesa 09 - 2 chairs
                        new DinningTable("10", 2) // Mesa 10 - 2 chairs
                );

                dinningTableRepository.saveAll(tables);
                logger.info("Tables initialized successfully.");
            } else {
                logger.info("Tables already exist in the database. Skipping initialization.");
            }
        };
    }
}
