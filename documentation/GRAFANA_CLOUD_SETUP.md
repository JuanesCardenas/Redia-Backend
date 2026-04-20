# Configuración de Grafana Cloud con Prometheus

## 📋 Resumen de los cambios realizados

Se han realizado los siguientes cambios para integrar tu aplicación Spring Boot con Grafana Cloud:

### 1. **Dependencias agregadas** (`build.gradle`)
- `io.micrometer:micrometer-core`: Proporciona soporte core para métricas

### 2. **Configuración actualizada** (`application.yml`)
- Agregada sección `prometheus.remote-write` para enviar métricas a Grafana Cloud
- Configuradas variables de entorno para credenciales

### 3. **Configuración local** (`application-local.yml`)
- Agregadas variables de Grafana Cloud (vacías para desarrollo)

### 4. **Clase de configuración** (`PrometheusConfig.java`)
- Personalización del registro de métricas
- Tags comunes para identificar la aplicación

---

## 🔧 Pasos para configurar Grafana Cloud

### Opción 1: Variables de Entorno (Recomendado para Producción)

Define estas variables en tu sistema o en tu `.env`:

```bash
# Variables obligatorias para Grafana Cloud
export GRAFANA_CLOUD_PROMETHEUS_URL="https://prometheus-blocks-prod-us-central1.grafana.net/api/prom/push"
export GRAFANA_CLOUD_USERNAME="YOUR_GRAFANA_USER_ID"
export GRAFANA_CLOUD_PASSWORD="YOUR_GRAFANA_API_TOKEN"
export GRAFANA_CLOUD_TOKEN="Bearer YOUR_GRAFANA_API_TOKEN"
```

**Cómo obtener estas credenciales:**

1. Accede a [Grafana Cloud](https://grafana.com/cloud)
2. Ve a **Stack Details** → **Prometheus**
3. Busca la sección **Remote Write** o **Push URL**
4. Copia:
   - **URL**: Prometheus Push endpoint
   - **Username**: Tu Grafana Stack ID o Username
   - **Password/Token**: API Token generado en **Security**

### Opción 2: Archivo de propiedades local

Edita `application-local.yml`:

```yaml
grafana:
  cloud:
    prometheus-url: "https://prometheus-blocks-prod-us-central1.grafana.net/api/prom/push"
    username: "YOUR_USER_ID"
    password: "YOUR_API_TOKEN"
    token: "Bearer YOUR_API_TOKEN"
```

### Opción 3: Docker Compose

Si usas Docker, en tu `docker-compose.yml`:

```yaml
environment:
  - GRAFANA_CLOUD_PROMETHEUS_URL=https://prometheus-blocks-prod-us-central1.grafana.net/api/prom/push
  - GRAFANA_CLOUD_USERNAME=YOUR_USER_ID
  - GRAFANA_CLOUD_PASSWORD=YOUR_API_TOKEN
  - GRAFANA_CLOUD_TOKEN=Bearer YOUR_API_TOKEN
```

---

## ✅ Verificar que funciona

### 1. Compilar y ejecutar la aplicación

```bash
./gradlew clean build
./gradlew bootRun
```

### 2. Acceder al endpoint de métricas

```bash
curl http://localhost:8080/actuator/prometheus
```

Deberías ver métricas en formato Prometheus.

### 3. Verificar en Grafana Cloud

1. Ve a **Explore** en Grafana Cloud
2. Selecciona tu datasource de Prometheus
3. Busca por `restaurante_app` o alguna métrica de tu aplicación
4. Las métricas deberían aparecer después de 1-2 minutos

---

## 🔍 Solución de problemas

### ❌ Las métricas no aparecen en Grafana Cloud

**Posibles causas:**

1. **Credenciales incorrectas**
   - Verifica que `GRAFANA_CLOUD_PROMETHEUS_URL`, `GRAFANA_CLOUD_USERNAME` y `GRAFANA_CLOUD_PASSWORD` sean correctos
   - El token debe tener permisos de `metrics:write`

2. **Endpoint `/actuator/prometheus` no accesible**
   - Verifica que el puerto 8080 esté abierto
   - Prueba: `curl http://localhost:8080/actuator/health`

3. **Firewall o proxy bloqueando la conexión**
   - Verifica que la aplicación pueda hacer peticiones HTTPS a Grafana Cloud
   - Prueba desde la terminal: `curl -I https://prometheus-blocks-prod-us-central1.grafana.net`

4. **Métricas no se están recolectando**
   - Asegúrate que la aplicación está recibiendo tráfico
   - Spring Boot genera métricas automáticas para HTTP requests, base de datos, JVM, etc.

---

## 📊 Métricas disponibles por defecto

Tu aplicación expone automáticamente:

- **JVM**: Memoria, garbage collection, threads
- **HTTP**: Requests, latencia, errores
- **Base de datos**: Conexiones activas, queries
- **Sistema**: CPU, uptime

---

## 🚀 Próximos pasos

1. Compilar: `./gradlew clean build`
2. Ejecutar con perfil local: `./gradlew bootRun --args='--spring.profiles.active=local'`
3. Configurar variables de Grafana Cloud
4. Verificar que las métricas aparecen en Grafana Cloud
5. Crear dashboards y alertas en Grafana Cloud

