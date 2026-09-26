package com.la_navaja.backend.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.la_navaja.backend.infrastructure.schemas.ScheduleExceptionSchema;

public interface ScheduleExceptionJpaRepository extends JpaRepository<ScheduleExceptionSchema, Long> {
}
