package com.la_navaja.backend.infrastructure.controllers.advice;

import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.exceptions.DomainException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
    return ResponseEntity.status(ex.getHttpStatus()).body(new ErrorResponse(ex.getMessage(), null));
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    ConstraintViolationException.class
  })
  public ResponseEntity<ErrorResponse> handleRequestValidationException(Exception ex) {
    log.warn("Invalid request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ErrorMessages.INVALID_REQUEST_MESSAGE, null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ErrorMessages.INTERNAL_ERROR_MESSAGE, null));
  }
}
