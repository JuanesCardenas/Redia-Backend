package com.redia.back.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de Prometheus para integración con Grafana Cloud.
 * Permite enviar métricas a través de Remote Write.
 */
@Configuration
public class PrometheusMetricsConfig {

    @Value("${grafana.cloud.prometheus-url:}")
    private String grafanaPrometheusUrl;

    @Value("${grafana.cloud.username:}")
    private String grafanaUsername;

    @Value("${grafana.cloud.password:}")
    private String grafanaPassword;

    @Value("${grafana.cloud.token:}")
    private String grafanaToken;

    /**
     * Personaliza el registro de métricas para Prometheus
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "restaurante-app",
                        "environment", getEnvironment()
                );
    }

    /**
     * Bean para RestTemplate usado en cliente HTTP
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Obtiene el ambiente actual
     */
    private String getEnvironment() {
        String activeProfile = System.getProperty("spring.profiles.active", "development");
        return activeProfile.isEmpty() ? "development" : activeProfile;
    }
}


