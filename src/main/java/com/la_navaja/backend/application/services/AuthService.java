package com.la_navaja.backend.application.services;

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
import com.la_navaja.backend.domain.models.User;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final InvitationTokenRepository invitationTokenRepository;
  private final Clock clock;

  public User signIn(SignInRequest request) {
    User user =
        userRepository
            .findByEmail(request.email().toLowerCase())
            .orElseThrow(
                () -> new UnauthorizedException(ErrorMessages.INVALID_CREDENTIALS_MESSAGE));

    if (user.password() == null || !passwordHasher.matches(request.password(), user.password())) {
      throw new UnauthorizedException(ErrorMessages.INVALID_CREDENTIALS_MESSAGE);
    }

    return user;
  }

  public void setPassword(SetPasswordRequest request) {
    InvitationToken invitationToken =
        invitationTokenRepository
            .findByToken(request.token())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ErrorMessages.INVITATION_TOKEN_NOT_FOUND_MESSAGE));

    if (invitationToken.used() || invitationToken.expiresAt().isBefore(LocalDateTime.now(clock))) {
      throw new BadRequestException(ErrorMessages.INVITATION_TOKEN_INVALID_MESSAGE);
    }

    User user =
        userRepository
            .findById(invitationToken.userId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        ErrorMessages.INVITATION_TOKEN_NOT_FOUND_MESSAGE));

    String hashedPassword = passwordHasher.hash(request.password());
    User updatedUser =
        User.builder()
            .id(user.id())
            .firstName(user.firstName())
            .lastNames(user.lastNames())
            .email(user.email())
            .password(hashedPassword)
            .role(user.role())
            .phone(user.phone())
            .createdAt(user.createdAt())
            .updatedAt(user.updatedAt())
            .build();
    userRepository.save(updatedUser);

    InvitationToken usedToken =
        InvitationToken.builder()
            .id(invitationToken.id())
            .token(invitationToken.token())
            .userId(invitationToken.userId())
            .expiresAt(invitationToken.expiresAt())
            .used(true)
            .build();
    invitationTokenRepository.save(usedToken);
  }
}
