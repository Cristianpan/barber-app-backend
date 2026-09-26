package com.la_navaja.backend.infrastructure.controllers;

import com.la_navaja.backend.application.dtos.request.SignInRequest;
import com.la_navaja.backend.application.dtos.response.SignInResponse;
import com.la_navaja.backend.application.services.AuthService;
import com.la_navaja.backend.domain.constants.ErrorMessages;
import com.la_navaja.backend.domain.models.User;
import com.la_navaja.backend.infrastructure.config.security.JwtService;
import com.la_navaja.backend.infrastructure.controllers.advice.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  @Value("${app.security.cookie-secure}")
  private boolean cookieSecure;

  @Operation(summary = "Sign in with email and password")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Authentication successful",
        content = @Content(schema = @Schema(implementation = SignInResponse.class)),
        headers =
            @Header(
                name = "Set-Cookie",
                description = "HttpOnly session token cookie",
                schema = @Schema(type = "string"))),
    @ApiResponse(
        responseCode = "400",
        description = ErrorMessages.INVALID_REQUEST_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = ErrorMessages.INVALID_CREDENTIALS_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "500",
        description = ErrorMessages.INTERNAL_ERROR_MESSAGE,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  @PostMapping("/sign-in")
  public ResponseEntity<SignInResponse> signIn(
      @Valid @RequestBody SignInRequest request, HttpServletResponse response) {

    User user = authService.signIn(request);

    String token = jwtService.createToken(user);

    ResponseCookie cookie =
        ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Lax")
            .path("/")
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(
        new SignInResponse(user.email(), user.firstName(), user.lastNames(), user.role()));
  }
}
