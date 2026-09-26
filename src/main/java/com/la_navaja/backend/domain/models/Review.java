package com.la_navaja.backend.domain.models;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Review(
    Long id,
    Long clientId,
    int rating,
    String comment,
    ReviewStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
