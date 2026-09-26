package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.BusinessHourSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessHourJpaRepository extends JpaRepository<BusinessHourSchema, Long> {}
