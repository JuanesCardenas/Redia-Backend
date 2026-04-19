package com.redia.back.dto;

/**
 * DTO para representar un correo electrónico.
 * Soporta tanto texto plano como HTML.
 *
 * @param subject Asunto del correo
 * @param body Contenido del correo (texto plano o HTML)
 * @param recipient Dirección de correo del destinatario
 * @param isHtml True si el body es HTML, false si es texto plano
 */
public record EmailDTO(String subject,
                       String body,
                       String recipient,
                       boolean isHtml) {

    // Constructor para compatibilidad hacia atrás (defecto: texto plano)
    public EmailDTO(String subject, String body, String recipient) {
        this(subject, body, recipient, false);
    }
}