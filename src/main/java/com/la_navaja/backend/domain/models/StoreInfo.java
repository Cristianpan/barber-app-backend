package com.la_navaja.backend.domain.models;

import java.time.LocalDateTime;

import lombok.Builder;

/** Información pública de la tienda. Fila única; los horarios viven en {@link BusinessHour}. */
@Builder
public record StoreInfo(
        Long id,
        String name,
        String history,
        String aboutUs,
        String address,
        String phone,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
