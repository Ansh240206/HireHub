package com.hirehub.dto.profile;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ApplicantProfileRequest(
        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Location is required")
        String location,

        @NotBlank(message = "Summary is required")
        String summary,

        List<EducationRequest> education,
        List<ExperienceRequest> experience,
        List<String> skills
) {
}
