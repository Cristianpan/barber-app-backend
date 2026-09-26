package com.la_navaja.backend.infrastructure.repositories;

import com.la_navaja.backend.application.repositories.StoreInfoRepository;
import com.la_navaja.backend.domain.models.StoreInfo;
import com.la_navaja.backend.infrastructure.mappers.StoreInfoMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreInfoRepositoryImpl implements StoreInfoRepository {

  private final StoreInfoJpaRepository jpaRepository;
  private final StoreInfoMapper mapper;

  @Override
  public StoreInfo save(StoreInfo storeInfo) {
    return mapper.toModel(jpaRepository.save(mapper.toSchema(storeInfo)));
  }

  @Override
  public Optional<StoreInfo> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toModel);
  }

  @Override
  public List<StoreInfo> findAll() {
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
