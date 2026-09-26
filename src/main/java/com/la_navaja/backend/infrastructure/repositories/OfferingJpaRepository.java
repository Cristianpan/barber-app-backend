package com.la_navaja.backend.infrastructure.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.la_navaja.backend.infrastructure.schemas.OfferingSchema;

public interface OfferingJpaRepository extends JpaRepository<OfferingSchema, Long> {
}
