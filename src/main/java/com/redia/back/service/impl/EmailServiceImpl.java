package com.redia.back.service.impl;

import com.redia.back.dto.EmailDTO;
import com.redia.back.service.EmailService;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de correos electrónicos.
 * Soporta tanto HTML como texto plano.
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    @Async
    public void sendMail(EmailDTO emailDTO) {
        try {
            // Construir el email con soporte para HTML
            var emailBuilder = EmailBuilder.startingBlank()
                    .from("redia.serviciocliente@gmail.com")
                    .to(emailDTO.recipient())
                    .withSubject(emailDTO.subject());

            // Si es HTML, usar withHTMLText, sino usar withPlainText
            if (emailDTO.isHtml()) {
                emailBuilder.withHTMLText(emailDTO.body());
            } else {
                emailBuilder.withPlainText(emailDTO.body());
            }

            Email email = emailBuilder.buildEmail();

            // Enviar el correo
            try (Mailer mailer = MailerBuilder
                    .withSMTPServer("smtp.gmail.com", 587,
                            "redia.serviciocliente@gmail.com", "oyme mpun cund jhvb")
                    .withTransportStrategy(TransportStrategy.SMTP_TLS)
                    .withDebugLogging(true)
                    .buildMailer()) {

                mailer.sendMail(email);
                System.out.println("Correo enviado exitosamente a: " + emailDTO.recipient());
            }
        } catch (Exception e) {
            System.err.println("Error enviando correo a " + emailDTO.recipient() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}