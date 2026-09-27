package com.la_navaja.backend.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.la_navaja.backend.application.dtos.request.RegisterEmployeeRequest;
import com.la_navaja.backend.application.dtos.response.RegisterEmployeeResponse;
import com.la_navaja.backend.application.services.EmployeeService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.ResourceAlreadyExistsException;
import com.la_navaja.backend.domain.models.Role;
import com.la_navaja.backend.infrastructure.config.security.AuthenticatedUserProvider;
import com.la_navaja.backend.infrastructure.config.security.CookieAuthFilter;
import com.la_navaja.backend.infrastructure.config.security.JwtService;
import com.la_navaja.backend.infrastructure.config.security.SecurityConfig;
import com.la_navaja.backend.infrastructure.controllers.advice.GlobalExceptionHandler;
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

@WebMvcTest(EmployeeController.class)
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
class EmployeeControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private EmployeeService employeeService;
  @MockitoBean private JwtService jwtService;

  private static final RegisterEmployeeRequest VALID_REQUEST =
      new RegisterEmployeeRequest("Juan", "Pérez", "juan@example.com", "555-1234", null);

  @Nested
  class RegisterEmployeeTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRegisterEmployeeSuccessfully() throws Exception {
      RegisterEmployeeResponse response =
          new RegisterEmployeeResponse(
              1L,
              "Juan",
              "Pérez",
              "juan@example.com",
              Role.EMPLOYEE,
              "555-1234",
              LocalDateTime.of(2026, 1, 1, 10, 0));

      when(employeeService.registerEmployee(any(RegisterEmployeeRequest.class)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/employees")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.firstName").value("Juan"))
          .andExpect(jsonPath("$.lastNames").value("Pérez"))
          .andExpect(jsonPath("$.email").value("juan@example.com"))
          .andExpect(jsonPath("$.role").value("EMPLOYEE"))
          .andExpect(jsonPath("$.phone").value("555-1234"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidRequest() throws Exception {
      mockMvc
          .perform(post("/employees").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
      mockMvc
          .perform(
              post("/employees")
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
              post("/employees")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(ErrorMessages.FORBIDDEN_MESSAGE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
      when(employeeService.registerEmployee(any(RegisterEmployeeRequest.class)))
          .thenThrow(
              new ResourceAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS_MESSAGE));

      mockMvc
          .perform(
              post("/employees")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message").value(ErrorMessages.EMAIL_ALREADY_EXISTS_MESSAGE));
    }
  }
}
