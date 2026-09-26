package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.application.repositories.OfferingRepository;
import com.la_navaja.backend.domain.models.Offering;
import com.la_navaja.backend.infrastructure.mappers.OfferingMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfferingRepositoryImpl implements OfferingRepository {

  private final OfferingJpaRepository jpaRepository;
  private final OfferingMapper mapper;

  @Override
  public Offering save(Offering offering) {
    return mapper.toModel(jpaRepository.save(mapper.toSchema(offering)));
  }

  @Override
  public Optional<Offering> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toModel);
  }

  @Override
  public List<Offering> findAll() {
    return jpaRepository.findAll().stream().map(mapper::toModel).toList();
  }

  @Override
  public void deleteById(Long id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(Long id) {
    return jpaRepository.existsById(id);
  }
}
