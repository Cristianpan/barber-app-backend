package com.la_navaja.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Mapea el bloque {@code app.initial-admin.*} de {@code application.properties}. Todos los campos
 * son opcionales: si alguno está vacío el seeder no hace nada.
 */
@ConfigurationProperties(prefix = "app.initial-admin")
public record AdminProperties(
    @DefaultValue("") String firstName,
    @DefaultValue("") String lastNames,
    @DefaultValue("") String email,
    @DefaultValue("") String phone,
    @DefaultValue("") String password) {

  /** Verdadero solo cuando los campos mínimos están presentes. */
  public boolean isConfigured() {
    return !firstName.isBlank() && !lastNames.isBlank() && !email.isBlank() && !password.isBlank();
  }
}
