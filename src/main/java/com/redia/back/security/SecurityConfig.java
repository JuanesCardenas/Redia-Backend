package com.redia.back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Configuración principal de seguridad de la aplicación usando Spring Security.
 *
 * Define las reglas de autenticación, autorización, CORS y gestión de sesiones.
 * La aplicación utiliza autenticación stateless basada en JWT, por lo que no
 * se mantiene ningún estado de sesión en el servidor.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Define la cadena de filtros de seguridad principal de la aplicación.
     *
     * Configura las siguientes reglas:
     * CORS: usando la configuración definida en {@link #corsConfigurationSource()}
     * CSRF: deshabilitado, ya que se usa autenticación stateless con JWT
     * Sesiones: política STATELESS, no se crean sesiones en el servidor
     * Rutas públicas: autenticación, Swagger, Actuator (Prometheus y Health)
     * Resto de rutas: requieren autenticación JWT válida
     * 
     * @param http objeto de configuración de seguridad HTTP provisto por Spring
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la configuración de seguridad
     */

    // CADENA 1: Basic Auth para actuator (Grafana Cloud) - PRIORIDAD ALTA
    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/**") // Solo aplica a endpoints de actuator
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/prometheus", "/actuator/health").authenticated() // Requiere auth
                        .anyRequest().permitAll())
                .httpBasic(basic -> {
                }); // Usa Basic Auth (usuario/contraseña)

        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .crossOriginOpenerPolicy(coop -> coop
                                .policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.UNSAFE_NONE)))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**", // Endpoints de login y registro
                                "/api/chatbot/**", // Endpoints del chatbot (auth opcional)
                                "/api/orders/debug-all", // Temp debug
                                "/v3/api-docs/**", // Documentación OpenAPI
                                "/swagger-ui/**", // Interfaz Swagger UI
                                "/swagger-ui.html", // Página principal de Swagger
                                "/actuator/prometheus", // Métricas para Grafana/Prometheus
                                "/actuator/health" // Health check de Azure App Service
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define el encoder de contraseñas usado para cifrar y verificar contraseñas de
     * usuarios.
     *
     * Utiliza BCrypt con su factor de costo por defecto, lo que provee un balance
     * adecuado entre seguridad y rendimiento.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el {@link AuthenticationManager} como bean de Spring para ser
     * inyectado
     * en otros componentes que lo requieran, como el servicio de autenticación.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura las reglas de CORS (Cross-Origin Resource Sharing) para la
     * aplicación.
     *
     * Define los orígenes permitidos (frontends conocidos y Grafana Cloud),
     * los métodos HTTP aceptados, y permite el envío de credenciales en las
     * peticiones.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                // Entornos de desarrollo local
                "http://localhost:4200",
                "http://localhost:5173",
                // Frontends desplegados en Azure
                "https://redia-frontend-dubwawhkgwaefkgu.mexicocentral-01.azurewebsites.net",
                "https://polite-ground-04850fb1e.2.azurestaticapps.net",
                "https://thankful-pebble-01e3fc91e.2.azurestaticapps.net",
                // Grafana Cloud
                "https://rediarestaurante.grafana.net"));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD", "TRACE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}