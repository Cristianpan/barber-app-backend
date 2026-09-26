package com.la_navaja.backend.infrastructure.mappers;

import org.springframework.stereotype.Component;

import com.la_navaja.backend.domain.models.Client;
import com.la_navaja.backend.infrastructure.schemas.ClientSchema;

@Component
public class ClientMapper {

    public Client toModel(ClientSchema schema) {
        return Client.builder()
                .id(schema.getId())
                .firstName(schema.getFirstName())
                .lastNames(schema.getLastNames())
                .phone(schema.getPhone())
                .email(schema.getEmail())
                .createdAt(schema.getCreatedAt())
                .updatedAt(schema.getUpdatedAt())
                .build();
    }

    public ClientSchema toSchema(Client model) {
        return ClientSchema.builder()
                .id(model.id())
                .firstName(model.firstName())
                .lastNames(model.lastNames())
                .phone(model.phone())
                .email(model.email())
                .build();
    }
}
