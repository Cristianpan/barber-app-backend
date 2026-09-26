package com.la_navaja.backend.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record Offering(
        Long id,
        String name,
        String normalizedName,
        String description,
        BigDecimal price,
        int durationMinutes,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
