package com.la_navaja.backend.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordRequest(
    @NotBlank String token, @NotBlank @Size(min = 8, max = 20) String password) {}
