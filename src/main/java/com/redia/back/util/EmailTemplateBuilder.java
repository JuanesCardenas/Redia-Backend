package com.redia.back.util;

/**
 * Constructor de templates HTML para correos con los estilos de Redia.
 * Colores de la marca:
 * - Primary (Morado oscuro): #612D53
 * - Secondary (Morado): #853953
 * - Tertiary (Gris oscuro): #2C2C2C
 * - Background (Gris claro): #f3f4f4
 */
public class EmailTemplateBuilder {

    private static final String PRIMARY_COLOR = "#612D53";
    private static final String SECONDARY_COLOR = "#853953";
    private static final String TERTIARY_COLOR = "#2C2C2C";
    private static final String BACKGROUND_COLOR = "#f3f4f4";
    private static final String WHITE = "#ffffff";

    /**
     * Template de bienvenida para nuevos usuarios
     */
    public static String welcomeTemplate(String nombre) {
        return buildTemplate(
                "¡Bienvenido a Redia!",
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>¡Bienvenido a Redia!</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Hola <strong>" + nombre + "</strong>,<br><br>" +
                        "Nos complace mucho que te unas a nuestra comunidad. Tu registro ha sido completado exitosamente." +
                        "</p>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Ya puedes acceder a la plataforma para:" +
                        "</p>" +
                        "<ul style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.8;'>" +
                        "<li>🍽️ Realizar reservas en Redia</li>" +
                        "<li>📋 Ver menús actualizados</li>" +
                        "<li>🎟️ Disfrutar de promociones exclusivas</li>" +
                        "</ul>" +
                        "<div style='margin-top: 30px; padding: 20px; background-color: " + BACKGROUND_COLOR + "; " +
                        "border-left: 4px solid " + SECONDARY_COLOR + "; border-radius: 4px;'>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0; font-size: 14px;'>" +
                        "<strong>💡 Tip:</strong> Si tienes alguna duda, no dudes en contactarnos. Estamos aquí para ayudarte." +
                        "</p>" +
                        "</div>"
        );
    }

    /**
     * Template para verificación de código
     */
    public static String verificationCodeTemplate(String nombre, String codigo) {
        return buildTemplate(
                "Tu código de verificación",
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>Código de Verificación</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Hola <strong>" + nombre + "</strong>,<br><br>" +
                        "Para continuar con la recuperación de tu contraseña, usa el siguiente código:" +
                        "</p>" +
                        "<div style='text-align: center; margin: 30px 0;'>" +
                        "<div style='background-color: " + PRIMARY_COLOR + "; color: " + WHITE + "; " +
                        "font-size: 32px; font-weight: bold; padding: 20px; letter-spacing: 8px; " +
                        "border-radius: 8px; font-family: monospace;'>" +
                        codigo +
                        "</div>" +
                        "</div>" +
                        "<p style='font-size: 14px; color: #999; text-align: center;'>" +
                        "Este código expira en 10 minutos" +
                        "</p>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Si no solicitaste este código, puedes ignorar este correo." +
                        "</p>"
        );
    }

    /**
     * Template para cambio de contraseña exitoso
     */
    public static String passwordChangeTemplate(String nombre) {
        return buildTemplate(
                "Tu contraseña ha sido actualizada",
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>✓ Contraseña Actualizada</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Hola <strong>" + nombre + "</strong>,<br><br>" +
                        "Tu contraseña ha sido actualizada correctamente." +
                        "</p>" +
                        "<div style='margin-top: 30px; padding: 20px; background-color: " + SECONDARY_COLOR + "; " +
                        "border-radius: 4px;'>" +
                        "<p style='color: " + WHITE + "; margin: 0; font-size: 14px;'>" +
                        "⚠️ <strong>Por tu seguridad:</strong> Si no realizaste este cambio, por favor contacta con nosotros de inmediato." +
                        "</p>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6; margin-top: 20px;'>" +
                        "Puedes acceder a tu cuenta con tu nueva contraseña." +
                        "</p>"
        );
    }

    /**
     * Template para confirmación de reserva
     */
    public static String reservationConfirmationTemplate(String nombre, String fechaReserva,
                                                         String horaReserva, int cantidadPersonas) {
        return buildTemplate(
                "Tu reserva ha sido confirmada",
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>✓ Reserva Confirmada</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Hola <strong>" + nombre + "</strong>,<br><br>" +
                        "¡Excelente! Tu reserva en Redia ha sido confirmada." +
                        "</p>" +
                        "<div style='margin: 30px 0; padding: 20px; background-color: " + BACKGROUND_COLOR + "; " +
                        "border-left: 4px solid " + SECONDARY_COLOR + "; border-radius: 4px;'>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0 0 10px 0;'>" +
                        "<strong>📅 Fecha:</strong> " + fechaReserva +
                        "</p>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0 0 10px 0;'>" +
                        "<strong>⏰ Hora:</strong> " + horaReserva +
                        "</p>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0;'>" +
                        "<strong>👥 Personas:</strong> " + cantidadPersonas +
                        "</p>" +
                        "</div>" +
                        "<p style='font-size: 14px; color: #999;'>" +
                        "Si necesitas cancelar o modificar tu reserva, por favor hazlo con al menos 24 horas de anticipación." +
                        "</p>"
        );
    }

    /**
     * Template para cancelación de reserva
     */
    public static String reservationCancelledTemplate(String nombre, String fechaReserva, String horaReserva) {
        return buildTemplate(
                "Tu reserva ha sido cancelada",
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>Reserva Cancelada</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "Hola <strong>" + nombre + "</strong>,<br><br>" +
                        "Tu reserva ha sido cancelada." +
                        "</p>" +
                        "<div style='margin: 30px 0; padding: 20px; background-color: " + BACKGROUND_COLOR + "; " +
                        "border-left: 4px solid " + SECONDARY_COLOR + "; border-radius: 4px;'>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0 0 10px 0;'>" +
                        "<strong>📅 Fecha:</strong> " + fechaReserva +
                        "</p>" +
                        "<p style='color: " + TERTIARY_COLOR + "; margin: 0;'>" +
                        "<strong>⏰ Hora:</strong> " + horaReserva +
                        "</p>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        "¡Nos encantaría volverte a ver en Redia pronto!" +
                        "</p>"
        );
    }

    /**
     * Template genérico para mensajes
     */
    public static String genericTemplate(String titulo, String mensaje) {
        return buildTemplate(
                titulo,
                "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h1 style='color: " + PRIMARY_COLOR + "; margin: 0;'>" + titulo + "</h1>" +
                        "</div>" +
                        "<p style='font-size: 16px; color: " + TERTIARY_COLOR + "; line-height: 1.6;'>" +
                        mensaje +
                        "</p>"
        );
    }

    /**
     * Construye el template HTML completo con estilos
     */
    private static String buildTemplate(String titulo, String contenido) {
        return "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>" + titulo + "</title>" +
                "    <style>" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                "        body { font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', 'Roboto', sans-serif; " +
                "               background-color: " + BACKGROUND_COLOR + "; line-height: 1.6; }" +
                "        .container { max-width: 600px; margin: 0 auto; background-color: " + WHITE + "; " +
                "                     border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }" +
                "        .header { background-color: " + PRIMARY_COLOR + "; color: " + WHITE + "; " +
                "                  padding: 40px 20px; text-align: center; }" +
                "        .header h1 { font-size: 28px; margin: 0; font-weight: 600; }" +
                "        .content { padding: 40px 30px; }" +
                "        .footer { background-color: " + BACKGROUND_COLOR + "; padding: 20px; " +
                "                  text-align: center; border-top: 1px solid #ddd; }" +
                "        .footer p { font-size: 12px; color: #999; margin: 5px 0; }" +
                "        .cta-button { display: inline-block; background-color: " + SECONDARY_COLOR + "; " +
                "                      color: " + WHITE + "; padding: 12px 30px; border-radius: 4px; " +
                "                      text-decoration: none; font-weight: 600; margin-top: 20px; }" +
                "        .cta-button:hover { background-color: " + PRIMARY_COLOR + "; }" +
                "        a { color: " + SECONDARY_COLOR + "; text-decoration: none; }" +
                "        a:hover { text-decoration: underline; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🍽️ REDIA</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                contenido +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p><strong>Redia | Experiencia Gastronómica Inolvidable</strong></p>" +
                "            <p>Síguenos en nuestras redes sociales</p>" +
                "            <p style='margin-top: 15px; font-size: 11px;'>" +
                "                Este es un correo automático. Por favor no respondas a este mensaje.<br>" +
                "                Para contactar con nosotros, visita nuestro sitio web." +
                "            </p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}

