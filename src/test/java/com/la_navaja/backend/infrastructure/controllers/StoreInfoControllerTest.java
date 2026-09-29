package com.la_navaja.backend.infrastructure.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.la_navaja.backend.application.dtos.request.SetStoreInfoRequest;
import com.la_navaja.backend.application.dtos.response.SetStoreInfoResponse;
import com.la_navaja.backend.application.services.StoreInfoService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
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

@WebMvcTest(StoreInfoController.class)
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
class StoreInfoControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private StoreInfoService storeInfoService;
  @MockitoBean private JwtService jwtService;

  private static final SetStoreInfoRequest VALID_REQUEST =
      new SetStoreInfoRequest(
          "La Navaja", "Calle 1", "555-0100", "info@lanavaja.com", "Historia", "Sobre nosotros");

  @Nested
  class SetStoreInfoTests {

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSetStoreInfoSuccessfully() throws Exception {
      SetStoreInfoResponse response =
          new SetStoreInfoResponse(
              1L,
              "La Navaja",
              "Calle 1",
              "555-0100",
              "info@lanavaja.com",
              "Historia",
              "Sobre nosotros",
              LocalDateTime.of(2026, 1, 1, 10, 0));

      when(storeInfoService.setStoreInfo(any(SetStoreInfoRequest.class))).thenReturn(response);

      mockMvc
          .perform(
              put("/store/info")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("La Navaja"))
          .andExpect(jsonPath("$.email").value("info@lanavaja.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidRequest() throws Exception {
      mockMvc
          .perform(put("/store/info").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
      mockMvc
          .perform(
              put("/store/info")
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
              put("/store/info")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(VALID_REQUEST)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(ErrorMessages.FORBIDDEN_MESSAGE));
    }
  }
}
