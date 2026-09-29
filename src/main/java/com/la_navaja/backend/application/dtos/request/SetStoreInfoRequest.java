package com.la_navaja.backend.application.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SetStoreInfoRequest(
    @NotBlank String name,
    String address,
    String phone,
    @Email String email,
    String history,
    String aboutUs) {}
