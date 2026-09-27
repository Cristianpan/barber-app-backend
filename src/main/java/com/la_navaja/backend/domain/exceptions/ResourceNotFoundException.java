package com.la_navaja.backend.domain.exceptions;

public class ResourceNotFoundException extends DomainException {

  public ResourceNotFoundException(String message) {
    super(message, 404);
  }
}
