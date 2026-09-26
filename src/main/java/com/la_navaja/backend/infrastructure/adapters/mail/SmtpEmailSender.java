package com.la_navaja.backend.infrastructure.adapters.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.la_navaja.backend.application.ports.EmailDefinition;
import com.la_navaja.backend.application.ports.EmailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final EmailTemplateRenderer renderer;
    private final String from;

    SmtpEmailSender(
            JavaMailSender javaMailSender,
            EmailTemplateRenderer renderer,
            @Value("${app.mail.from}") String from) {
        this.javaMailSender = javaMailSender;
        this.renderer = renderer;
        this.from = from;
    }

    @Override
    public <T> void send(String to, EmailDefinition<T> email, T data) {
        String body = renderer.render(email.template(), email.toModel(data));
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(email.subject(data));
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("No se pudo enviar el correo a " + to, e);
        }
    }
}
