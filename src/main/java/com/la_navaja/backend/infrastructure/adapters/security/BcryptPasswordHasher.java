package com.la_navaja.backend.infrastructure.adapters.security;

import com.la_navaja.backend.application.ports.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BcryptPasswordHasher implements PasswordHasher {

  private final PasswordEncoder passwordEncoder;

  @Override
  public boolean matches(String rawPassword, String hashedPassword) {
    return passwordEncoder.matches(rawPassword, hashedPassword);
  }
}
