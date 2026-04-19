# 📧 Guía de Uso de Email Templates HTML en Redia

## 📝 Descripción General

Se ha implementado un sistema completo de email templates HTML embebidos con los colores y estilos de la marca Redia. Los correos ahora son profesionales y atractivos, similar a los que envía Nu en el ejemplo que proporcionaste.

## 🎨 Colores de la Marca Utilizados

- **Primary (Morado Oscuro)**: `#612D53` - Encabezados y botones principales
- **Secondary (Morado)**: `#853953` - Detalles y acentos
- **Tertiary (Gris Oscuro)**: `#2C2C2C` - Texto principal
- **Background (Gris Claro)**: `#f3f4f4` - Fondo de contenedores
- **White**: `#ffffff` - Fondo principal

## 📚 Templates Disponibles

### 1. **Welcome Template** (Bienvenida)

```java
// Uso
String htmlContent = EmailTemplateBuilder.welcomeTemplate("Juan");

new EmailDTO(
    "¡Bienvenido a Redia!",
    htmlContent,
    "usuario@example.com",
    true  // true = HTML
);
```

**Cuándo se usa**: Al registrarse un nuevo usuario

**Características**:
- Título de bienvenida
- Información sobre características disponibles
- Tips útiles en recuadro destacado

---

### 2. **Verification Code Template** (Código de Verificación)

```java
// Código generado aleatoriamente
String codigo = "654321";

String htmlContent = EmailTemplateBuilder.verificationCodeTemplate(
    "Juan",
    codigo
);

new EmailDTO(
    "Recuperación de contraseña",
    htmlContent,
    "usuario@example.com",
    true
);
```

**Cuándo se usa**: Enviarlo al solicitar recuperar contraseña

**Características**:
- Código de verificación en grande y destacado
- Tiempo de expiración (10 minutos)
- Estilos llamativos con monospace font

---

### 3. **Password Change Template** (Cambio de Contraseña)

```java
// Uso
String htmlContent = EmailTemplateBuilder.passwordChangeTemplate("Juan");

new EmailDTO(
    "Tu contraseña se ha actualizado correctamente",
    htmlContent,
    "usuario@example.com",
    true
);
```

**Cuándo se usa**: 
- Después de cambiar contraseña
- Después de resetearla
- Después de recuperarla

**Características**:
- Confirmación visual con ✓
- Alerta de seguridad en color rojo
- Instrucciones claras

---

### 4. **Reservation Confirmation Template** (Confirmación de Reserva)

```java
// Uso
String htmlContent = EmailTemplateBuilder.reservationConfirmationTemplate(
    "Juan",              // nombre
    "18/04/2026",        // fecha
    "20:30",             // hora
    4                    // cantidad de personas
);

new EmailDTO(
    "¡Tu reserva está confirmada!",
    htmlContent,
    "usuario@example.com",
    true
);
```

**Cuándo se usa**: Cuando se confirma una reserva

**Características**:
- Todos los detalles de la reserva en un recuadro
- Ícono de confirmación ✓
- Instrucciones de llegada

---

### 5. **Reservation Cancelled Template** (Cancelación de Reserva)

```java
// Uso
String htmlContent = EmailTemplateBuilder.reservationCancelledTemplate(
    "Juan",              // nombre
    "18/04/2026",        // fecha
    "20:30"              // hora
);

new EmailDTO(
    "Tu reserva ha sido cancelada",
    htmlContent,
    "usuario@example.com",
    true
);
```

**Cuándo se usa**: Cuando se cancela una reserva

**Características**:
- Información clara de la cancelación
- Recuadro con detalles
- Invitación a visitarnos nuevamente

---

### 6. **Generic Template** (Template Genérico)

```java
// Uso para mensajes personalizados
String htmlContent = EmailTemplateBuilder.genericTemplate(
    "Título del Correo",
    "<p>Contenido HTML personalizado</p>" +
    "<p>Puede incluir etiquetas HTML</p>"
);

new EmailDTO(
    "Asunto del Correo",
    htmlContent,
    "usuario@example.com",
    true
);
```

**Cuándo se usa**: Para cualquier otro tipo de correo

---

## 💡 Cambios en EmailDTO

El `EmailDTO` ahora tiene un parámetro adicional `isHtml`:

```java
// Viejo (aún funciona por compatibilidad)
new EmailDTO("Asunto", "Cuerpo", "correo@example.com");
// Por defecto isHtml = false (texto plano)

// Nuevo para HTML
new EmailDTO("Asunto", "Cuerpo HTML", "correo@example.com", true);
```

---

## 🔧 Cambios en EmailServiceImpl

El servicio ahora soporta automáticamente HTML:

```java
if (emailDTO.isHtml()) {
    emailBuilder.withHTMLText(emailDTO.body());
} else {
    emailBuilder.withPlainText(emailDTO.body());
}
```

---

## 📧 Servicios Actualizados

Los siguientes servicios ya fueron actualizados para usar los nuevos templates:

### **AuthServiceImpl**
- ✅ Correo de bienvenida (register)
- ✅ Correo de código de verificación (sendVerificationCode)
- ✅ Correo de cambio de contraseña (recoverPassword, resetPassword, changePassword)

### **ReservationServiceImpl**
- ✅ Correo de confirmación de reserva (confirmarReserva)
- ✅ Correo de cancelación (cancelarYNotificar)
- ✅ Correo de finalización (finalizarReserva)

---

## 🎯 Próximas Actualizaciones (Opcional)

Si quieres agregar más templates:

1. **Correo de recordatorio de reserva** (24h antes)
2. **Correo de menu del día**
3. **Correo de promoción especial**
4. **Correo de feedback después de la visita**

---

## 🧪 Pruebas

Para probar localmente:

```bash
./gradlew build
./gradlew bootRun
```

Los correos se enviarán a través de Gmail con las credenciales configuradas en `EmailServiceImpl`.

---

## 📋 Checklist de Implementación

- ✅ Clase `EmailTemplateBuilder` creada
- ✅ `EmailDTO` actualizado con soporte HTML
- ✅ `EmailServiceImpl` actualizado para enviar HTML
- ✅ `AuthServiceImpl` actualizado con templates
- ✅ `ReservationServiceImpl` actualizado con templates
- ✅ Proyecto compilado correctamente
- ✅ Compilación sin errores

¡Todo listo para enviar correos HTML profesionales! 🚀

