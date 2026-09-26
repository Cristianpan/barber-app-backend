package com.la_navaja.backend.application.repositories;

import com.la_navaja.backend.domain.models.ScheduleException;
import java.util.List;
import java.util.Optional;

public interface ScheduleExceptionRepository {

  ScheduleException save(ScheduleException scheduleException);

  Optional<ScheduleException> findById(Long id);

  List<ScheduleException> findAll();

  void deleteById(Long id);

  boolean existsById(Long id);
}
