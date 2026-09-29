package com.la_navaja.backend.application.dtos.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record DayScheduleResponse(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime breakStart,
    LocalTime breakEnd) {}
