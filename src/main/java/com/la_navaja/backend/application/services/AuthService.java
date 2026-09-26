package com.la_navaja.backend.application.services;

import com.la_navaja.backend.application.dtos.request.SignInRequest;
import com.la_navaja.backend.application.ports.PasswordHasher;
import com.la_navaja.backend.application.repositories.UserRepository;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.UnauthorizedException;
import com.la_navaja.backend.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;

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
}
