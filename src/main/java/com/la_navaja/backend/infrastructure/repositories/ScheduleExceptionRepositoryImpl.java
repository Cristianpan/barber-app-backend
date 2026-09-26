package com.la_navaja.backend.infrastructure.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.la_navaja.backend.application.repositories.ScheduleExceptionRepository;
import com.la_navaja.backend.domain.models.ScheduleException;
import com.la_navaja.backend.infrastructure.mappers.ScheduleExceptionMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ScheduleExceptionRepositoryImpl implements ScheduleExceptionRepository {

    private final ScheduleExceptionJpaRepository jpaRepository;
    private final ScheduleExceptionMapper mapper;

    @Override
    public ScheduleException save(ScheduleException scheduleException) {
        return mapper.toModel(jpaRepository.save(mapper.toSchema(scheduleException)));
    }

    @Override
    public Optional<ScheduleException> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<ScheduleException> findAll() {
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
