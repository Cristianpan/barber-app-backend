package com.la_navaja.backend.domain.models;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;

/** Horario distinto para una fecha concreta. Sin horas = cerrado todo el día. */
@Builder
public record ScheduleException(
    Long id, LocalDate date, LocalTime startTime, LocalTime endTime, String reason) {}
