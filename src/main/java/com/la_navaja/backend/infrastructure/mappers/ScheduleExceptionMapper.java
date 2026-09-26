package com.la_navaja.backend.infrastructure.mappers;

import org.springframework.stereotype.Component;

import com.la_navaja.backend.domain.models.ScheduleException;
import com.la_navaja.backend.infrastructure.schemas.ScheduleExceptionSchema;

@Component
public class ScheduleExceptionMapper {

    public ScheduleException toModel(ScheduleExceptionSchema schema) {
        return ScheduleException.builder()
                .id(schema.getId())
                .date(schema.getDate())
                .startTime(schema.getStartTime())
                .endTime(schema.getEndTime())
                .reason(schema.getReason())
                .build();
    }

    public ScheduleExceptionSchema toSchema(ScheduleException model) {
        return ScheduleExceptionSchema.builder()
                .id(model.id())
                .date(model.date())
                .startTime(model.startTime())
                .endTime(model.endTime())
                .reason(model.reason())
                .build();
    }
}
