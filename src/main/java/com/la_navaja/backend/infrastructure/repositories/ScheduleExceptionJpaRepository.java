package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.ScheduleExceptionSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleExceptionJpaRepository
    extends JpaRepository<ScheduleExceptionSchema, Long> {}
