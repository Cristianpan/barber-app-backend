package com.la_navaja.backend.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
    name = "cookieAuth",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "token")
public class OpenApiConfig {

  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
        .info(
            new Info()
                .title("La Navaja API")
                .version("0.1.0")
                .description("Backend de gestión de personal, servicios y citas."));
  }

  @Bean
  public GroupedOpenApi authApi() {
    return GroupedOpenApi.builder().group("auth").pathsToMatch("/auth/**").build();
  }

  @Bean
  public GroupedOpenApi employeesApi() {
    return GroupedOpenApi.builder().group("employees").pathsToMatch("/employees/**").build();
  }

  @Bean
  public GroupedOpenApi servicesApi() {
    return GroupedOpenApi.builder().group("services").pathsToMatch("/services/**").build();
  }

  @Bean
  public GroupedOpenApi appointmentsApi() {
    return GroupedOpenApi.builder().group("appointments").pathsToMatch("/appointments/**").build();
  }

  @Bean
  public GroupedOpenApi storeApi() {
    return GroupedOpenApi.builder().group("store").pathsToMatch("/store/**").build();
  }
}
