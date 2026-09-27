package com.la_navaja.backend.domain.exceptions;

public class BadRequestException extends DomainException {

  public BadRequestException(String message) {
    super(message, 400);
  }
}
