#!/bin/bash

# Script de verificación rápida de configuración Grafana + Prometheus
# Uso: bash verify-prometheus.sh

echo "🔍 VERIFICANDO CONFIGURACIÓN DE PROMETHEUS..."
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para verificar
check() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ $1${NC}"
  else
    echo -e "${RED}✗ $1${NC}"
  fi
}

# 1. Verificar puerto
echo "1️⃣  Verificando puerto 8080..."
netstat -tuln | grep 8080 > /dev/null || ss -tuln | grep 8080 > /dev/null
check "Aplicación corriendo en puerto 8080"
echo ""

# 2. Verificar endpoint de health
echo "2️⃣  Verificando endpoint de health..."
curl -s http://localhost:8080/actuator/health | grep -q "UP"
check "Endpoint /actuator/health respondiendo"
echo ""

# 3. Verificar endpoint de prometheus
echo "3️⃣  Verificando endpoint de Prometheus (requiere credenciales)..."
if [ -z "$PROM_USER" ] || [ -z "$PROM_PASS" ]; then
  echo -e "${YELLOW}⚠ Variables PROM_USER o PROM_PASS no definidas, usando defaults...${NC}"
  PROM_USER="grafana_scraper"
  PROM_PASS="dummy_password_for_local"
fi

METRICS=$(curl -s -u "$PROM_USER:$PROM_PASS" http://localhost:8080/actuator/prometheus | head -1)
if [ -n "$METRICS" ]; then
  echo -e "${GREEN}✓ Endpoint /actuator/prometheus respondiendo${NC}"
  echo "  Primeras métricas:"
  curl -s -u "$PROM_USER:$PROM_PASS" http://localhost:8080/actuator/prometheus | grep -v "^#" | head -5
else
  echo -e "${RED}✗ Endpoint /actuator/prometheus no respondiendo${NC}"
fi
echo ""

# 4. Verificar credenciales Grafana
echo "4️⃣  Variables de entorno para Grafana Cloud:"
if [ -n "$GRAFANA_CLOUD_PROMETHEUS_URL" ]; then
  echo -e "${GREEN}✓ GRAFANA_CLOUD_PROMETHEUS_URL definida${NC}"
else
  echo -e "${RED}✗ GRAFANA_CLOUD_PROMETHEUS_URL no definida${NC}"
fi

if [ -n "$GRAFANA_CLOUD_USERNAME" ]; then
  echo -e "${GREEN}✓ GRAFANA_CLOUD_USERNAME definida${NC}"
else
  echo -e "${RED}✗ GRAFANA_CLOUD_USERNAME no definida${NC}"
fi

if [ -n "$GRAFANA_CLOUD_PASSWORD" ]; then
  echo -e "${GREEN}✓ GRAFANA_CLOUD_PASSWORD definida${NC}"
else
  echo -e "${RED}✗ GRAFANA_CLOUD_PASSWORD no definida${NC}"
fi
echo ""

# 5. Verificar conectividad a Grafana Cloud
echo "5️⃣  Verificando conectividad a Grafana Cloud..."
if [ -n "$GRAFANA_CLOUD_PROMETHEUS_URL" ]; then
  curl -s -I "$GRAFANA_CLOUD_PROMETHEUS_URL" > /dev/null 2>&1
  check "Puedo conectar a Grafana Cloud"
else
  echo -e "${YELLOW}⚠ GRAFANA_CLOUD_PROMETHEUS_URL no configurada${NC}"
fi
echo ""

# 6. Resumen
echo "================================================"
echo "✅ VERIFICACIÓN COMPLETADA"
echo "================================================"
echo ""
echo "📌 PRÓXIMOS PASOS:"
echo "1. Ve a https://grafana.com/cloud"
echo "2. Configura tu Datasource Prometheus con:"
echo "   URL: http://tu-dominio:8080/actuator/prometheus"
echo "   User: $PROM_USER"
echo "   Pass: (tu PROM_PASS)"
echo "3. En Explore, busca métricas like: jvm_memory_used, http_requests, etc."
echo ""

