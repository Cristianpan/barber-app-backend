package com.la_navaja.backend.domain.exceptions;

public class UnauthorizedException extends DomainException {

  public UnauthorizedException(String message) {
    super(message, 401);
  }
}
