package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.application.repositories.AppointmentRepository;
import com.la_navaja.backend.domain.models.Appointment;
import com.la_navaja.backend.infrastructure.mappers.AppointmentMapper;
import com.la_navaja.backend.infrastructure.schemas.AppointmentSchema;
import com.la_navaja.backend.infrastructure.schemas.ClientSchema;
import com.la_navaja.backend.infrastructure.schemas.OfferingSchema;
import com.la_navaja.backend.infrastructure.schemas.UserSchema;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppointmentRepositoryImpl implements AppointmentRepository {

  private final AppointmentJpaRepository jpaRepository;
  private final AppointmentMapper mapper;
  private final EntityManager entityManager;

  @Override
  public Appointment save(Appointment appointment) {
    AppointmentSchema schema = mapper.toSchema(appointment);
    schema.setClient(entityManager.getReference(ClientSchema.class, appointment.clientId()));
    schema.setEmployee(entityManager.getReference(UserSchema.class, appointment.employeeId()));
    schema.setOffering(entityManager.getReference(OfferingSchema.class, appointment.offeringId()));
    return mapper.toModel(jpaRepository.save(schema));
  }

  @Override
  public Optional<Appointment> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toModel);
  }

  @Override
  public List<Appointment> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toModel).toList();
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return jpaRepository.existsById(id);
  }
}
