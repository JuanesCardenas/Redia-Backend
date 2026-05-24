package com.redia.back.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de Prometheus y Grafana Cloud para producción.
 * Expone métricas en /actuator/prometheus para que Grafana Cloud las recolecte.
 */
@Configuration
public class PrometheusMetricsConfig {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsConfig.class);

    /**
     * Personaliza el registro de métricas con tags comunes
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        logger.info("Inicializando configuración de métricas de Prometheus");
        return registry -> registry.config()
                .commonTags(
                        "application", "redia-backend",
                        "service", "restaurants",
                        "version", "1.0.0"
                );
    }
    
    /**
     * Bean para RestTemplate usado en cliente HTTP
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}



