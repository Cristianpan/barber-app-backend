package com.la_navaja.backend.application.repositories;

import java.util.List;
import java.util.Optional;

import com.la_navaja.backend.domain.models.StoreInfo;

public interface StoreInfoRepository {

    StoreInfo save(StoreInfo storeInfo);

    Optional<StoreInfo> findById(Long id);

    List<StoreInfo> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
