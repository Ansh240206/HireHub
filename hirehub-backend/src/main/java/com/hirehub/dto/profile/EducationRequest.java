package com.hirehub.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record EducationRequest(
        @NotBlank(message = "Institution is required")
        String institution,

        @NotBlank(message = "Degree is required")
        String degree,

        Integer startYear,
        Integer endYear
) {
}
