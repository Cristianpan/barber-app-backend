package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.InvitationToken;
import com.la_navaja.backend.infrastructure.schemas.InvitationTokenSchema;
import org.springframework.stereotype.Component;

@Component
public class InvitationTokenMapper {

  public InvitationToken toModel(InvitationTokenSchema schema) {
    return InvitationToken.builder()
        .id(schema.getId())
        .token(schema.getToken())
        .userId(schema.getUserId())
        .expiresAt(schema.getExpiresAt())
        .used(schema.isUsed())
        .build();
  }

  public InvitationTokenSchema toSchema(InvitationToken model) {
    return InvitationTokenSchema.builder()
        .id(model.id())
        .token(model.token())
        .userId(model.userId())
        .expiresAt(model.expiresAt())
        .used(model.used())
        .build();
  }
}
