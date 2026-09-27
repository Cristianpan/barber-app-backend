package com.la_navaja.backend.domain.models;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record InvitationToken(
    Long id, String token, Long userId, LocalDateTime expiresAt, boolean used) {}
