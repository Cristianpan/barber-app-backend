package com.la_navaja.backend.application.dtos.request;

import com.la_navaja.backend.domain.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterEmployeeRequest(
    @NotBlank String firstName,
    @NotBlank String lastNames,
    @NotBlank @Email String email,
    @NotBlank String phone,
    Role role) {}
