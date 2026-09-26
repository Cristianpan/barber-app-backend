package com.la_navaja.backend.domain.models;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record User(
    Long id,
    String firstName,
    String lastNames,
    String email,
    String password,
    Role role,
    String phone,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
