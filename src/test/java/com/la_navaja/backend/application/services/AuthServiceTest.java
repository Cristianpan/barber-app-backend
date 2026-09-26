package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.SignInRequest;
import com.la_navaja.backend.application.ports.PasswordHasher;
import com.la_navaja.backend.application.repositories.UserRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.UnauthorizedException;
import com.la_navaja.backend.domain.models.Role;
import com.la_navaja.backend.domain.models.User;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordHasher passwordHasher;

  @InjectMocks private AuthService authService;

  @Nested
  class SignInTests {

    @Test
    void shouldReturnUserData() {
      User user =
          User.builder()
              .id(1L)
              .email("admin@example.com")
              .firstName("John")
              .lastNames("Doe")
              .role(Role.ADMIN)
              .password("hashed")
              .build();

      when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
      when(passwordHasher.matches("secret", "hashed")).thenReturn(true);

      User result = authService.signIn(new SignInRequest("admin@example.com", "secret"));

      assertThat(result.email()).isEqualTo("admin@example.com");
      assertThat(result.firstName()).isEqualTo("John");
      assertThat(result.lastNames()).isEqualTo("Doe");
      assertThat(result.role()).isEqualTo(Role.ADMIN);
      assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldRejectInvalidCredentials_whenEmailNotFound() {
      when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> authService.signIn(new SignInRequest("unknown@example.com", "secret")))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage(ErrorMessages.INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    void shouldRejectInvalidCredentials_whenPasswordMismatch() {
      User user =
          User.builder()
              .id(2L)
              .email("admin@example.com")
              .password("hashed")
              .role(Role.ADMIN)
              .build();

      when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
      when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

      assertThatThrownBy(() -> authService.signIn(new SignInRequest("admin@example.com", "wrong")))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage(ErrorMessages.INVALID_CREDENTIALS_MESSAGE);
    }
  }
}
