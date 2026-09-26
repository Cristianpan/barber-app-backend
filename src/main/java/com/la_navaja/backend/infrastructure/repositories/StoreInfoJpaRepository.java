package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.StoreInfoSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreInfoJpaRepository extends JpaRepository<StoreInfoSchema, Long> {}
