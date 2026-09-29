package com.la_navaja.backend.application.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SetStoreScheduleRequest(@NotEmpty List<@Valid DayScheduleRequest> days) {}
