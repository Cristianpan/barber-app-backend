package com.la_navaja.backend.application.services;

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
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

  private final UserRepository userRepository;
  private final InvitationTokenRepository invitationTokenRepository;
  private final EmailSender emailSender;
  private final EmailDefinition<InvitationEmailData> invitationEmail;
  private final String frontendUrl;
  private final Clock clock;

  public EmployeeService(
      UserRepository userRepository,
      InvitationTokenRepository invitationTokenRepository,
      EmailSender emailSender,
      EmailDefinition<InvitationEmailData> invitationEmail,
      @Value("${app.frontend-url}") String frontendUrl,
      Clock clock) {
    this.userRepository = userRepository;
    this.invitationTokenRepository = invitationTokenRepository;
    this.emailSender = emailSender;
    this.invitationEmail = invitationEmail;
    this.frontendUrl = frontendUrl;
    this.clock = clock;
  }

  @Transactional
  public RegisterEmployeeResponse registerEmployee(RegisterEmployeeRequest request) {
    if (userRepository.findByEmail(request.email().toLowerCase()).isPresent()) {
      throw new ResourceAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS_MESSAGE);
    }

    Role role = request.role() != null ? request.role() : Role.EMPLOYEE;

    User user =
        userRepository.save(
            User.builder()
                .firstName(request.firstName())
                .lastNames(request.lastNames())
                .email(request.email().toLowerCase())
                .phone(request.phone())
                .role(role)
                .password(null)
                .build());

    String token = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(15);

    invitationTokenRepository.save(
        InvitationToken.builder()
            .token(token)
            .userId(user.id())
            .expiresAt(expiresAt)
            .used(false)
            .build());

    String invitationLink = frontendUrl + "/set-password?token=" + token;

    emailSender.send(
        user.email(), invitationEmail, new InvitationEmailData(user.firstName(), invitationLink));

    return new RegisterEmployeeResponse(
        user.id(),
        user.firstName(),
        user.lastNames(),
        user.email(),
        user.role(),
        user.phone(),
        user.createdAt());
  }
}
