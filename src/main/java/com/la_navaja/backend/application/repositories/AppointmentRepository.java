package com.la_navaja.backend.application.repositories;

import java.util.List;
import java.util.Optional;

import com.la_navaja.backend.domain.models.Appointment;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(Long id);

    List<Appointment> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
