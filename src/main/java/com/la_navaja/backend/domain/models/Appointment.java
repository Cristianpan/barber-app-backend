package com.la_navaja.backend.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * `durationMinutes` y `price` son copia del offering al agendar: editarlo no altera citas pasadas.
 */
@Builder
public record Appointment(
    Long id,
    Long clientId,
    Long employeeId,
    Long offeringId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    int durationMinutes,
    BigDecimal price,
    AppointmentStatus status,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
