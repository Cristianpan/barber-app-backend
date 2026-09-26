package com.la_navaja.backend.infrastructure.config.security;

import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.UnauthorizedException;
import com.la_navaja.backend.domain.models.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserProvider {

  public AuthenticatedUser getAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
      throw new UnauthorizedException(ErrorMessages.UNAUTHORIZED_MESSAGE);
    }

    return (AuthenticatedUser) authentication.getPrincipal();
  }
}
