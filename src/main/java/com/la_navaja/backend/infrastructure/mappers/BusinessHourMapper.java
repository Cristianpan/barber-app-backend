package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.BusinessHour;
import com.la_navaja.backend.infrastructure.schemas.BusinessHourSchema;
import org.springframework.stereotype.Component;

@Component
public class BusinessHourMapper {

  public BusinessHour toModel(BusinessHourSchema schema) {
    return BusinessHour.builder()
        .id(schema.getId())
        .dayOfWeek(schema.getDayOfWeek())
        .startTime(schema.getStartTime())
        .endTime(schema.getEndTime())
        .build();
  }

  public BusinessHourSchema toSchema(BusinessHour model) {
    return BusinessHourSchema.builder()
        .id(model.id())
        .dayOfWeek(model.dayOfWeek())
        .startTime(model.startTime())
        .endTime(model.endTime())
        .build();
  }
}
