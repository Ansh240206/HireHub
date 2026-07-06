package com.hirehub.dto.user;

import com.hirehub.enums.RoleName;

public record UserResponse(
        Long id,
        String name,
        String email,
        RoleName role,
        boolean enabled
) {
}
