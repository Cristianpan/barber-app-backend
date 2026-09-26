package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.UserSchema;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserSchema, Long> {

  Optional<UserSchema> findByEmail(String email);
}
