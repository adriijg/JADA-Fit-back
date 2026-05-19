package es.jadafit.jadafit_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendPasswordResetToken(String to, String token) {
        if (mailSender == null) {
            log.warn("Mail no configurado. Token para {}: {}", to, token);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("JADA FIT - Restablecer contrasena");
            message.setText("Has solicitado restablecer tu contrasena.\n\n"
                    + "Tu codigo de verificacion es: " + token + "\n\n"
                    + "Este codigo expira en 15 minutos.\n\n"
                    + "Si no has solicitado este cambio, ignora este mensaje.");

            mailSender.send(message);
            log.info("Email de restablecimiento enviado a {}", to);
        } catch (Exception e) {
            log.warn("No se pudo enviar email a {}. Token: {}", to, token, e);
        }
    }
}
