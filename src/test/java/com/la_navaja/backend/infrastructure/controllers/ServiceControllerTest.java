package com.la_navaja.backend.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.la_navaja.backend.application.dtos.request.CreateServiceRequest;
import com.la_navaja.backend.application.dtos.response.CreateServiceResponse;
import com.la_navaja.backend.application.services.ServiceService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.ResourceAlreadyExistsException;
import com.la_navaja.backend.infrastructure.config.security.AuthenticatedUserProvider;
import com.la_navaja.backend.infrastructure.config.security.CookieAuthFilter;
import com.la_navaja.backend.infrastructure.config.security.JwtService;
import com.la_navaja.backend.infrastructure.config.security.SecurityConfig;
import com.la_navaja.backend.infrastructure.controllers.advice.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ServiceController.class)
@Import({
  SecurityConfig.class,
  CookieAuthFilter.class,
  GlobalExceptionHandler.class,
  AuthenticatedUserProvider.class
})
@TestPropertySource(
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-bytes-long-ok-here",
      "app.jwt.expiration-ms=86400000",
      "app.security.cookie-secure=false"
    })
class ServiceControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private ServiceService serviceService;
  @MockitoBean private JwtService jwtService;

  private static final CreateServiceRequest VALID_REQUEST =
      new CreateServiceRequest("Corte", "Corte de cabello", 30, BigDecimal.valueOf(100));

  @Nested
  class CreateServiceTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateServiceSuccessfully() throws Exception {
      CreateServiceResponse response =
          new CreateServiceResponse(
              1L,
              "Corte",
              "Corte de cabello",
              30,
              BigDecimal.valueOf(100),
              true,
              LocalDateTime.of(2026, 1, 1, 10, 0));

      when(serviceService.createService(any(CreateServiceRequest.class))).thenReturn(response);

      mockMvc
          .perform(
              post("/services")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("Corte"))
          .andExpect(jsonPath("$.description").value("Corte de cabello"))
          .andExpect(jsonPath("$.durationMinutes").value(30))
          .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidRequest() throws Exception {
      mockMvc
          .perform(post("/services").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidRequest_whenDurationOrPriceIsNotPositive() throws Exception {
      CreateServiceRequest invalid =
          new CreateServiceRequest("Corte", "Corte de cabello", 0, BigDecimal.ZERO);

      mockMvc
          .perform(
              post("/services")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalid)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
      mockMvc
          .perform(
              post("/services")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(ErrorMessages.UNAUTHORIZED_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void shouldRejectRequestWithoutAdminRole() throws Exception {
      mockMvc
          .perform(
              post("/services")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(ErrorMessages.FORBIDDEN_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflict_whenServiceNameAlreadyExists() throws Exception {
      when(serviceService.createService(any(CreateServiceRequest.class)))
          .thenThrow(
              new ResourceAlreadyExistsException(
                  ErrorMessages.SERVICE_NAME_ALREADY_EXISTS_MESSAGE));

      mockMvc
          .perform(
              post("/services")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isConflict())
          .andExpect(
              jsonPath("$.message").value(ErrorMessages.SERVICE_NAME_ALREADY_EXISTS_MESSAGE));
    }
  }
}
