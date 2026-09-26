package com.la_navaja.backend.infrastructure.mappers;

import com.la_navaja.backend.domain.models.User;
import com.la_navaja.backend.infrastructure.schemas.UserSchema;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toModel(UserSchema schema) {
    return User.builder()
        .id(schema.getId())
        .firstName(schema.getFirstName())
        .lastNames(schema.getLastNames())
        .email(schema.getEmail())
        .password(schema.getPassword())
        .role(schema.getRole())
        .phone(schema.getPhone())
        .createdAt(schema.getCreatedAt())
        .updatedAt(schema.getUpdatedAt())
        .build();
  }

  public UserSchema toSchema(User model) {
    return UserSchema.builder()
        .id(model.id())
        .firstName(model.firstName())
        .lastNames(model.lastNames())
        .email(model.email())
        .password(model.password())
        .role(model.role())
        .phone(model.phone())
        .build();
  }
}
