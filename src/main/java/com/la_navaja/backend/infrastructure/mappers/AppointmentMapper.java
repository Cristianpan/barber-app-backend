package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.Appointment;
import com.la_navaja.backend.infrastructure.schemas.AppointmentSchema;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

  public Appointment toModel(AppointmentSchema schema) {
    return Appointment.builder()
        .id(schema.getId())
        .clientId(schema.getClient().getId())
        .employeeId(schema.getEmployee().getId())
        .offeringId(schema.getOffering().getId())
        .startAt(schema.getStartAt())
        .endAt(schema.getEndAt())
        .durationMinutes(schema.getDurationMinutes())
        .price(schema.getPrice())
        .status(schema.getStatus())
        .notes(schema.getNotes())
        .createdAt(schema.getCreatedAt())
        .updatedAt(schema.getUpdatedAt())
        .build();
  }

  /** Solo campos escalares: las referencias las pone el repositorio con {@code getReference}. */
  public AppointmentSchema toSchema(Appointment model) {
    return AppointmentSchema.builder()
        .id(model.id())
        .startAt(model.startAt())
        .endAt(model.endAt())
        .durationMinutes(model.durationMinutes())
        .price(model.price())
        .status(model.status())
        .notes(model.notes())
        .build();
  }
}
