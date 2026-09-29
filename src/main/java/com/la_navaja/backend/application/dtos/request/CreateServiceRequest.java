package com.la_navaja.backend.application.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateServiceRequest(
    @NotBlank String name,
    @NotBlank String description,
    @Positive int durationMinutes,
    @Positive BigDecimal price) {}
