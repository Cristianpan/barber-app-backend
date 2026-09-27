package com.la_navaja.backend.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.la_navaja.backend.application.dtos.request.RegisterEmployeeRequest;
import com.la_navaja.backend.application.dtos.response.RegisterEmployeeResponse;
import com.la_navaja.backend.application.ports.EmailDefinition;
import com.la_navaja.backend.application.ports.EmailSender;
import com.la_navaja.backend.application.ports.InvitationEmailData;
import com.la_navaja.backend.application.repositories.InvitationTokenRepository;
import com.la_navaja.backend.application.repositories.UserRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.ResourceAlreadyExistsException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private InvitationTokenRepository invitationTokenRepository;
  @Mock private EmailSender emailSender;

  @SuppressWarnings("unchecked")
  @Mock
  private EmailDefinition<InvitationEmailData> invitationEmail;

  private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneId.of("UTC"));

  private EmployeeService employeeService;

  void setUp(String frontendUrl) {
    employeeService =
        new EmployeeService(
            userRepository,
            invitationTokenRepository,
            emailSender,
            invitationEmail,
            frontendUrl,
            clock);
  }

  @Nested
  class RegisterEmployeeTests {

    @Test
    void shouldRegisterEmployeeSuccessfully() {
      setUp("http://localhost:3000");
      RegisterEmployeeRequest request =
          new RegisterEmployeeRequest(
              "Juan", "Pérez", "juan@example.com", "555-1234", Role.EMPLOYEE);

      User savedUser =
          User.builder()
              .id(1L)
              .firstName("Juan")
              .lastNames("Pérez")
              .email("juan@example.com")
              .role(Role.EMPLOYEE)
              .phone("555-1234")
              .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
              .build();

      when(userRepository.findByEmail("juan@example.com")).thenReturn(Optional.empty());
      when(userRepository.save(any(User.class))).thenReturn(savedUser);
      when(invitationTokenRepository.save(any(InvitationToken.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      RegisterEmployeeResponse response = employeeService.registerEmployee(request);

      assertThat(response.id()).isEqualTo(1L);
      assertThat(response.firstName()).isEqualTo("Juan");
      assertThat(response.lastNames()).isEqualTo("Pérez");
      assertThat(response.email()).isEqualTo("juan@example.com");
      assertThat(response.role()).isEqualTo(Role.EMPLOYEE);
      assertThat(response.phone()).isEqualTo("555-1234");

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().password()).isNull();

      ArgumentCaptor<InvitationToken> tokenCaptor = ArgumentCaptor.forClass(InvitationToken.class);
      verify(invitationTokenRepository).save(tokenCaptor.capture());
      InvitationToken savedToken = tokenCaptor.getValue();
      assertThat(savedToken.userId()).isEqualTo(1L);
      assertThat(savedToken.used()).isFalse();
      assertThat(savedToken.expiresAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 15));

      ArgumentCaptor<InvitationEmailData> emailDataCaptor =
          ArgumentCaptor.forClass(InvitationEmailData.class);
      verify(emailSender)
          .send(eq("juan@example.com"), eq(invitationEmail), emailDataCaptor.capture());
      assertThat(emailDataCaptor.getValue().firstName()).isEqualTo("Juan");
      assertThat(emailDataCaptor.getValue().invitationLink())
          .startsWith("http://localhost:3000/set-password?token=");
    }

    @Test
    void shouldAssignEmployeeRoleByDefault_whenRoleIsNull() {
      setUp("http://localhost:3000");
      RegisterEmployeeRequest request =
          new RegisterEmployeeRequest("Ana", "García", "ana@example.com", "555-9999", null);

      User savedUser =
          User.builder()
              .id(2L)
              .firstName("Ana")
              .lastNames("García")
              .email("ana@example.com")
              .role(Role.EMPLOYEE)
              .phone("555-9999")
              .createdAt(LocalDateTime.now())
              .build();

      when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
      when(userRepository.save(any(User.class))).thenReturn(savedUser);
      when(invitationTokenRepository.save(any(InvitationToken.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      employeeService.registerEmployee(request);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void shouldRejectRegistration_whenEmailAlreadyExists() {
      setUp("http://localhost:3000");
      RegisterEmployeeRequest request =
          new RegisterEmployeeRequest("Carlos", "López", "existing@example.com", "555-0001", null);

      User existing =
          User.builder().id(5L).email("existing@example.com").role(Role.EMPLOYEE).build();

      when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

      assertThatThrownBy(() -> employeeService.registerEmployee(request))
          .isInstanceOf(ResourceAlreadyExistsException.class)
          .hasMessage(ErrorMessages.EMAIL_ALREADY_EXISTS_MESSAGE);
    }
  }
}
