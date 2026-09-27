package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.infrastructure.schemas.InvitationTokenSchema;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationTokenJpaRepository extends JpaRepository<InvitationTokenSchema, Long> {

  Optional<InvitationTokenSchema> findByToken(String token);
}
