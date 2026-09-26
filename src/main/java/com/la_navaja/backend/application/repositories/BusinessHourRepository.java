package com.la_navaja.backend.application.repositories;

import java.util.List;
import java.util.Optional;

import com.la_navaja.backend.domain.models.BusinessHour;

public interface BusinessHourRepository {

    BusinessHour save(BusinessHour businessHour);

    Optional<BusinessHour> findById(Long id);

    List<BusinessHour> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
