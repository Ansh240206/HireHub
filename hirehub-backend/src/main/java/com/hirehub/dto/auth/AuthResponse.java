package com.hirehub.dto.auth;

import com.hirehub.enums.RoleName;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInMs,
        Long userId,
        String name,
        String email,
        RoleName role
) {
}
