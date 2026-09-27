package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.SetPasswordRequest;
import com.la_navaja.backend.application.dtos.request.SignInRequest;
import com.la_navaja.backend.application.ports.PasswordHasher;
import com.la_navaja.backend.application.repositories.InvitationTokenRepository;
import com.la_navaja.backend.application.repositories.UserRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.BadRequestException;
import com.la_navaja.backend.domain.exceptions.ResourceNotFoundException;
import com.la_navaja.backend.domain.exceptions.UnauthorizedException;
import com.la_navaja.backend.domain.models.InvitationToken;
import com.la_navaja.backend.domain.models.Role;
import com.la_navaja.backend.domain.models.User;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordHasher passwordHasher;

  @Mock private InvitationTokenRepository invitationTokenRepository;

  @Mock private Clock clock;

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

  @Nested
  class SetPasswordTests {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T12:00:00Z");
    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final LocalDateTime FUTURE =
        LocalDateTime.ofInstant(FIXED_INSTANT, ZONE).plusDays(1);
    private static final LocalDateTime PAST =
        LocalDateTime.ofInstant(FIXED_INSTANT, ZONE).minusDays(1);

    private void stubClock() {
      when(clock.instant()).thenReturn(FIXED_INSTANT);
      when(clock.getZone()).thenReturn(ZONE);
    }

    @Test
    void shouldSetPasswordWithValidToken() {
      stubClock();

      InvitationToken token =
          InvitationToken.builder()
              .id(10L)
              .token("invite-token")
              .userId(1L)
              .expiresAt(FUTURE)
              .used(false)
              .build();

      User user =
          User.builder()
              .id(1L)
              .email("employee@example.com")
              .firstName("Jane")
              .lastNames("Doe")
              .role(Role.EMPLOYEE)
              .password(null)
              .build();

      when(invitationTokenRepository.findByToken("invite-token")).thenReturn(Optional.of(token));
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(passwordHasher.hash("newpass1")).thenReturn("hashed-new");

      authService.setPassword(new SetPasswordRequest("invite-token", "newpass1"));

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().password()).isEqualTo("hashed-new");

      ArgumentCaptor<InvitationToken> tokenCaptor = ArgumentCaptor.forClass(InvitationToken.class);
      verify(invitationTokenRepository).save(tokenCaptor.capture());
      assertThat(tokenCaptor.getValue().used()).isTrue();
    }

    @Test
    void shouldRejectWhenTokenNotFound() {
      when(invitationTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> authService.setPassword(new SetPasswordRequest("bad-token", "newpass1")))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessage(ErrorMessages.INVITATION_TOKEN_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldRejectWhenTokenIsUsedOrExpired() {
      stubClock();

      InvitationToken usedToken =
          InvitationToken.builder()
              .id(11L)
              .token("used-token")
              .userId(1L)
              .expiresAt(FUTURE)
              .used(true)
              .build();

      InvitationToken expiredToken =
          InvitationToken.builder()
              .id(12L)
              .token("expired-token")
              .userId(1L)
              .expiresAt(PAST)
              .used(false)
              .build();

      when(invitationTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));
      when(invitationTokenRepository.findByToken("expired-token"))
          .thenReturn(Optional.of(expiredToken));

      assertThatThrownBy(
              () -> authService.setPassword(new SetPasswordRequest("used-token", "newpass1")))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(ErrorMessages.INVITATION_TOKEN_INVALID_MESSAGE);

      assertThatThrownBy(
              () -> authService.setPassword(new SetPasswordRequest("expired-token", "newpass1")))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(ErrorMessages.INVITATION_TOKEN_INVALID_MESSAGE);
    }
  }
}
