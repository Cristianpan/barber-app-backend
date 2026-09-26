package com.la_navaja.backend.domain.models;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record Client(
        Long id,
        String firstName,
        String lastNames,
        String phone,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
