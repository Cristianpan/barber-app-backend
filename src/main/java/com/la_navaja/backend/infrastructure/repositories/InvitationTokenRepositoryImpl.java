package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.application.repositories.InvitationTokenRepository;
import com.la_navaja.backend.domain.models.InvitationToken;
import com.la_navaja.backend.infrastructure.mappers.InvitationTokenMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InvitationTokenRepositoryImpl implements InvitationTokenRepository {

  private final InvitationTokenJpaRepository jpaRepository;
  private final InvitationTokenMapper mapper;

  @Override
  public InvitationToken save(InvitationToken token) {
    return mapper.toModel(jpaRepository.save(mapper.toSchema(token)));
  }

  @Override
  public Optional<InvitationToken> findByToken(String token) {
    return jpaRepository.findByToken(token).map(mapper::toModel);
  }
}
