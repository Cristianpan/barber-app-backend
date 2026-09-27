package com.la_navaja.backend.application.repositories;

import com.la_navaja.backend.domain.models.InvitationToken;
import java.util.Optional;

public interface InvitationTokenRepository {

  InvitationToken save(InvitationToken token);

  Optional<InvitationToken> findByToken(String token);
}
