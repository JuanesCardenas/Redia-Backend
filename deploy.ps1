# Script de deployment para Grafana Cloud - Windows PowerShell
# Uso: .\deploy.ps1

Write-Host "🚀 DEPLOYMENT SCRIPT - Redia Backend + Grafana Cloud" -ForegroundColor Green
Write-Host ""

# Colores
$GREEN = "Green"
$RED = "Red"
$YELLOW = "Yellow"
$BLUE = "Cyan"

# 1. Compilación
Write-Host "1️⃣  Compilando proyecto..." -ForegroundColor $BLUE
./gradlew.bat clean build -x test

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en la compilación" -ForegroundColor $RED
    exit 1
}
Write-Host "✅ Compilación exitosa" -ForegroundColor $GREEN
Write-Host ""

# 2. Verificar JAR
Write-Host "2️⃣  Verificando JAR generado..." -ForegroundColor $BLUE
$jarPath = ".\build\libs\Redia-Backend-1.0-SNAPSHOT.jar"
if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length / 1MB
    Write-Host "✅ JAR generado: $jarPath ($([math]::Round($jarSize, 2)) MB)" -ForegroundColor $GREEN
} else {
    Write-Host "❌ JAR no encontrado en $jarPath" -ForegroundColor $RED
    exit 1
}
Write-Host ""

# 3. Mostrar variables necesarias
Write-Host "3️⃣  Variables de entorno necesarias:" -ForegroundColor $BLUE
Write-Host ""
Write-Host "Usa estos comandos para definir variables en PowerShell:" -ForegroundColor $YELLOW
Write-Host ""
Write-Host '$env:DB_URL = "jdbc:mariadb://tu-host:3306/redia"' -ForegroundColor $YELLOW
Write-Host '$env:DB_USERNAME = "usuario"' -ForegroundColor $YELLOW
Write-Host '$env:DB_PASSWORD = "contraseña"' -ForegroundColor $YELLOW
Write-Host '$env:JWT_SECRET = "tu-secreto-largo"' -ForegroundColor $YELLOW
Write-Host '$env:PROM_USER = "prometheus_scraper"' -ForegroundColor $YELLOW
Write-Host '$env:PROM_PASS = "contraseña-prometheus"' -ForegroundColor $YELLOW
Write-Host '$env:SPRING_PROFILES_ACTIVE = "prod"' -ForegroundColor $YELLOW
Write-Host ""

# 4. Opciones de ejecución
Write-Host "4️⃣  Opciones para ejecutar:" -ForegroundColor $BLUE
Write-Host ""
Write-Host "OPCIÓN A - Ejecución local (desarrollo):" -ForegroundColor $YELLOW
Write-Host "./gradlew bootRun" -ForegroundColor $YELLOW
Write-Host ""
Write-Host "OPCIÓN B - Ejecutar JAR compilado:" -ForegroundColor $YELLOW
Write-Host "java -jar build\libs\Redia-Backend-1.0-SNAPSHOT.jar" -ForegroundColor $YELLOW
Write-Host ""
Write-Host "OPCIÓN C - Ejecutar JAR con perfil de producción:" -ForegroundColor $YELLOW
Write-Host 'java -Dspring.profiles.active=prod -jar build\libs\Redia-Backend-1.0-SNAPSHOT.jar' -ForegroundColor $YELLOW
Write-Host ""

# 5. Opciones de Docker
Write-Host "5️⃣  Si usas Docker:" -ForegroundColor $BLUE
Write-Host ""
Write-Host "docker build -t redia-backend:latest ." -ForegroundColor $YELLOW
Write-Host ""
Write-Host 'docker run -e SPRING_PROFILES_ACTIVE=prod `' -ForegroundColor $YELLOW
Write-Host '  -e DB_URL="jdbc:mariadb://db:3306/redia" `' -ForegroundColor $YELLOW
Write-Host '  -e PROM_USER="prometheus_scraper" `' -ForegroundColor $YELLOW
Write-Host '  -e PROM_PASS="contraseña" `' -ForegroundColor $YELLOW
Write-Host '  -p 8080:8080 redia-backend:latest' -ForegroundColor $YELLOW
Write-Host ""

# 6. Verificación
Write-Host "6️⃣  Para verificar después de desplegar:" -ForegroundColor $BLUE
Write-Host ""
Write-Host "Endpoint de health:" -ForegroundColor $YELLOW
Write-Host "curl http://localhost:8080/actuator/health" -ForegroundColor $YELLOW
Write-Host ""
Write-Host "Endpoint de métrica (requiere credenciales):" -ForegroundColor $YELLOW
Write-Host 'curl -u prometheus_scraper:CONTRASEÑA http://localhost:8080/actuator/prometheus' -ForegroundColor $YELLOW
Write-Host ""

Write-Host "================================================" -ForegroundColor $GREEN
Write-Host "📌 PRÓXIMOS PASOS:" -ForegroundColor $GREEN
Write-Host "================================================" -ForegroundColor $GREEN
Write-Host "1. Define las variables de entorno arriba ⬆️" -ForegroundColor $BLUE
Write-Host "2. Ejecuta la aplicación (OPCIÓN A, B o C)" -ForegroundColor $BLUE
Write-Host "3. Ve a Grafana Cloud y configura el datasource" -ForegroundColor $BLUE
Write-Host "4. Usa http://localhost:8080/actuator/prometheus en Grafana" -ForegroundColor $BLUE
Write-Host "5. Con credenciales: prometheus_scraper / TU_PROM_PASS" -ForegroundColor $BLUE
Write-Host ""

