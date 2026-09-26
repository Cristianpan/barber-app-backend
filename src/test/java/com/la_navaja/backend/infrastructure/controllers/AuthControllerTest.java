package com.la_navaja.backend.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.la_navaja.backend.application.dtos.request.SignInRequest;
import com.la_navaja.backend.application.services.AuthService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.UnauthorizedException;
import com.la_navaja.backend.domain.models.Role;
import com.la_navaja.backend.domain.models.User;
import com.la_navaja.backend.infrastructure.config.security.AuthenticatedUserProvider;
import com.la_navaja.backend.infrastructure.config.security.CookieAuthFilter;
import com.la_navaja.backend.infrastructure.config.security.JwtService;
import com.la_navaja.backend.infrastructure.config.security.SecurityConfig;
import com.la_navaja.backend.infrastructure.controllers.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(AuthController.class)
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
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private AuthService authService;
  @MockitoBean private JwtService jwtService;

  @Nested
  class SignInTests {

    @Test
    void shouldSignInSuccessfully() throws Exception {
      User user =
          User.builder()
              .id(1L)
              .email("admin@example.com")
              .firstName("John")
              .lastNames("Doe")
              .role(Role.ADMIN)
              .password("hashed")
              .build();

      when(authService.signIn(any(SignInRequest.class))).thenReturn(user);
      when(jwtService.createToken(user)).thenReturn("jwt-token-value");

      MvcResult result =
          mockMvc
              .perform(
                  post("/auth/sign-in")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(
                          objectMapper.writeValueAsString(
                              new SignInRequest("admin@example.com", "secret"))))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.email").value("admin@example.com"))
              .andExpect(jsonPath("$.firstName").value("John"))
              .andExpect(jsonPath("$.lastNames").value("Doe"))
              .andExpect(jsonPath("$.role").value("ADMIN"))
              .andReturn();

      String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
      assertThat(setCookieHeader).isNotNull().contains("token=jwt-token-value");
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
      when(authService.signIn(any(SignInRequest.class)))
          .thenThrow(new UnauthorizedException(ErrorMessages.INVALID_CREDENTIALS_MESSAGE));

      mockMvc
          .perform(
              post("/auth/sign-in")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(
                          new SignInRequest("bad@example.com", "wrong"))))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_CREDENTIALS_MESSAGE));
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
      mockMvc
          .perform(post("/auth/sign-in").contentType(MediaType.APPLICATION_JSON).content("{}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(ErrorMessages.INVALID_REQUEST_MESSAGE));
    }
  }

  @Nested
  class ProtectedEndpointTests {

    @Test
    void shouldRejectRequestToProtectedEndpointWithoutToken() throws Exception {
      mockMvc
          .perform(get("/any-protected-path"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message").value(ErrorMessages.UNAUTHORIZED_MESSAGE));
    }
  }
}
