package com.la_navaja.backend.infrastructure.mappers;

import org.springframework.stereotype.Component;

import com.la_navaja.backend.domain.models.Offering;
import com.la_navaja.backend.infrastructure.schemas.OfferingSchema;

@Component
public class OfferingMapper {

    public Offering toModel(OfferingSchema schema) {
        return Offering.builder()
                .id(schema.getId())
                .name(schema.getName())
                .normalizedName(schema.getNormalizedName())
                .description(schema.getDescription())
                .price(schema.getPrice())
                .durationMinutes(schema.getDurationMinutes())
                .enabled(schema.isEnabled())
                .createdAt(schema.getCreatedAt())
                .updatedAt(schema.getUpdatedAt())
                .build();
    }

    public OfferingSchema toSchema(Offering model) {
        return OfferingSchema.builder()
                .id(model.id())
                .name(model.name())
                .normalizedName(model.normalizedName())
                .description(model.description())
                .price(model.price())
                .durationMinutes(model.durationMinutes())
                .enabled(model.enabled())
                .build();
    }
}
