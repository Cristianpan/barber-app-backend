package com.la_navaja.backend.domain.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;

/**
 * Intervalo de trabajo de la empresa en un día de la semana. El hueco entre dos intervalos es el
 * descanso.
 */
@Builder
public record BusinessHour(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {}
