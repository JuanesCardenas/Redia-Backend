# Multi-stage build para optimizar tamaño
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar archivos del proyecto
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

# Compilar
RUN ./gradlew clean build -x test

# Imagen final
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /app/build/libs/Redia-Backend-1.0-SNAPSHOT.jar app.jar

# Crear usuario no-root para seguridad
RUN addgroup -g 1000 spring && adduser -D -u 1000 -G spring spring
USER spring

# Puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080
ENV PROM_USER=prometheus_scraper
ENV PROM_PASS=default_password

# Ejecutar aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

