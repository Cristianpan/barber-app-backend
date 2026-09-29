package com.la_navaja.backend.application.services;

import com.la_navaja.backend.application.dtos.request.SetStoreInfoRequest;
import com.la_navaja.backend.application.dtos.response.SetStoreInfoResponse;
import com.la_navaja.backend.application.repositories.StoreInfoRepository;
import com.la_navaja.backend.domain.models.StoreInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreInfoService {

  private final StoreInfoRepository storeInfoRepository;

  @Transactional
  public SetStoreInfoResponse setStoreInfo(SetStoreInfoRequest request) {
    Long existingId =
        storeInfoRepository.findAll().stream().findFirst().map(StoreInfo::id).orElse(null);

    StoreInfo storeInfo =
        storeInfoRepository.save(
            StoreInfo.builder()
                .id(existingId)
                .name(request.name())
                .address(request.address())
                .phone(request.phone())
                .email(request.email())
                .history(request.history())
                .aboutUs(request.aboutUs())
                .build());

    return new SetStoreInfoResponse(
        storeInfo.id(),
        storeInfo.name(),
        storeInfo.address(),
        storeInfo.phone(),
        storeInfo.email(),
        storeInfo.history(),
        storeInfo.aboutUs(),
        storeInfo.updatedAt());
  }
}
