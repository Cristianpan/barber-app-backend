package com.la_navaja.backend.application.repositories;

import java.util.List;
import java.util.Optional;

import com.la_navaja.backend.domain.models.Offering;

public interface OfferingRepository {

    Offering save(Offering offering);

    Optional<Offering> findById(Long id);

    List<Offering> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
