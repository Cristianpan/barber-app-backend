package com.la_navaja.backend.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Propiedades generales de la aplicación. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(@DefaultValue("http://localhost:3000") String frontendUrl, Cors cors) {

  public record Cors(@DefaultValue("http://localhost:3000") List<String> allowedOrigins) {}
}
