package com.la_navaja.backend.application.dtos.response;

import java.time.LocalDateTime;

public record SetStoreInfoResponse(
    Long id,
    String name,
    String address,
    String phone,
    String email,
    String history,
    String aboutUs,
    LocalDateTime updatedAt) {}
