package com.la_navaja.backend.application.dtos.response;

import com.la_navaja.backend.domain.models.Role;
import java.time.LocalDateTime;

public record RegisterEmployeeResponse(
    Long id,
    String firstName,
    String lastNames,
    String email,
    Role role,
    String phone,
    LocalDateTime createdAt) {}
