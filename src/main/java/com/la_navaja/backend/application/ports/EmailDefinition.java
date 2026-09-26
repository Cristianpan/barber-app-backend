package com.la_navaja.backend.application.ports;

import java.util.Map;

/**
 * Define un tipo de correo. {@code T} contiene los datos específicos del envío.
 *
 * <p>Para agregar un correo nuevo:
 * <ol>
 *   <li>Crear {@code XxxData} record con los datos que necesita la plantilla.
 *   <li>Implementar esta interfaz en {@code XxxEmail}.
 *   <li>Agregar la plantilla HTML en {@code templates/emails/xxx.html}.
 *   <li>Llamar a {@link EmailSender#send} desde el service.
 * </ol>
 */
public interface EmailDefinition<T> {

    /** Nombre del archivo en {@code templates/emails/} (sin extensión). */
    String template();

    String subject(T data);

    Map<String, Object> toModel(T data);
}
