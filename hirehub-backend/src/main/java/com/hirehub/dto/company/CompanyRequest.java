package com.hirehub.dto.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        String name,

        String description,
        String website,
        String location
) {
}
