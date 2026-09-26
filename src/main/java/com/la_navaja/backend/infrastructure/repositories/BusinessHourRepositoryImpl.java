package com.la_navaja.backend.infrastructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.la_navaja.backend.application.repositories.BusinessHourRepository;
import com.la_navaja.backend.domain.models.BusinessHour;
import com.la_navaja.backend.infrastructure.mappers.BusinessHourMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BusinessHourRepositoryImpl implements BusinessHourRepository {

    private final BusinessHourJpaRepository jpaRepository;
    private final BusinessHourMapper mapper;

    @Override
    public BusinessHour save(BusinessHour businessHour) {
        return mapper.toModel(jpaRepository.save(mapper.toSchema(businessHour)));
    }

    @Override
    public Optional<BusinessHour> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<BusinessHour> findAll() {
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
