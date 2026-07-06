package com.hirehub.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Size(min = 8, message = "New password must be at least 8 characters")
        @NotBlank(message = "New password is required")
        String newPassword
) {
}
