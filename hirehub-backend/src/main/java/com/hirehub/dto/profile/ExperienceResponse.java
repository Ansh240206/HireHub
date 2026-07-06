package com.hirehub.dto.profile;

public record ExperienceResponse(
        Long id,
        String companyName,
        String role,
        String duration
) {
}
