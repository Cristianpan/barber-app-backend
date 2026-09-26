package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.StoreInfo;
import com.la_navaja.backend.infrastructure.schemas.StoreInfoSchema;
import org.springframework.stereotype.Component;

@Component
public class StoreInfoMapper {

  public StoreInfo toModel(StoreInfoSchema schema) {
    return StoreInfo.builder()
        .id(schema.getId())
        .name(schema.getName())
        .history(schema.getHistory())
        .aboutUs(schema.getAboutUs())
        .address(schema.getAddress())
        .phone(schema.getPhone())
        .email(schema.getEmail())
        .createdAt(schema.getCreatedAt())
        .updatedAt(schema.getUpdatedAt())
        .build();
  }

  public StoreInfoSchema toSchema(StoreInfo model) {
    return StoreInfoSchema.builder()
        .id(model.id())
        .name(model.name())
        .history(model.history())
        .aboutUs(model.aboutUs())
        .address(model.address())
        .phone(model.phone())
        .email(model.email())
        .build();
  }
}
