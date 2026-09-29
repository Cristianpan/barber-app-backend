package com.la_navaja.backend.application.dtos.response;

import java.util.List;

public record SetStoreScheduleResponse(List<DayScheduleResponse> days) {}
