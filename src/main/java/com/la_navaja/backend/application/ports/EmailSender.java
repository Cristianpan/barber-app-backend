package com.la_navaja.backend.application.ports;

/** Puerto de envío de correo. La implementación vive en {@code infrastructure/adapters/mail}. */
public interface EmailSender {

    <T> void send(String to, EmailDefinition<T> email, T data);
}
