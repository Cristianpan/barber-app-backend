package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.OfferingSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferingJpaRepository extends JpaRepository<OfferingSchema, Long> {}
