package com.hirehub.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record ExperienceRequest(
        @NotBlank(message = "Company name is required")
        String companyName,

        @NotBlank(message = "Role is required")
        String role,

        String duration
) {
}
