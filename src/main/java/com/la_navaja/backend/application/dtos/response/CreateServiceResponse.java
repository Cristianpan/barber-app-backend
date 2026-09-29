package com.la_navaja.backend.application.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateServiceResponse(
    Long id,
    String name,
    String description,
    int durationMinutes,
    BigDecimal price,
    boolean enabled,
    LocalDateTime createdAt) {}
