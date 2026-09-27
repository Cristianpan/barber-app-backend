package com.la_navaja.backend.domain.exceptions;

public class ResourceAlreadyExistsException extends DomainException {

  public ResourceAlreadyExistsException(String message) {
    super(message, 409);
  }
}
