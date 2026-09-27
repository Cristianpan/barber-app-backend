package com.la_navaja.backend.infrastructure.controllers;

import com.la_navaja.backend.application.dtos.request.RegisterEmployeeRequest;
import com.la_navaja.backend.application.dtos.response.RegisterEmployeeResponse;
import com.la_navaja.backend.application.services.EmployeeService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.infrastructure.controllers.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Employees")
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @Operation(
      summary = "Register a new employee",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Employee registered successfully",
        content = @Content(schema = @Schema(implementation = RegisterEmployeeResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = ErrorMessages.INVALID_REQUEST_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = ErrorMessages.UNAUTHORIZED_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = ErrorMessages.FORBIDDEN_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = ErrorMessages.EMAIL_ALREADY_EXISTS_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = ErrorMessages.INTERNAL_ERROR_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<RegisterEmployeeResponse> registerEmployee(
      @Valid @RequestBody RegisterEmployeeRequest request) {
    RegisterEmployeeResponse response = employeeService.registerEmployee(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
