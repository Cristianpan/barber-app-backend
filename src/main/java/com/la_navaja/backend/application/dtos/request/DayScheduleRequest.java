package com.la_navaja.backend.application.dtos.request;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record DayScheduleRequest(
    @NotNull DayOfWeek dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    LocalTime breakStart,
    LocalTime breakEnd) {}
