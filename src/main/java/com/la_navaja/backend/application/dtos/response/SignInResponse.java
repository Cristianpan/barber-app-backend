package com.la_navaja.backend.application.dtos.response;

import com.la_navaja.backend.domain.models.Role;

public record SignInResponse(String email, String firstName, String lastNames, Role role) {}
