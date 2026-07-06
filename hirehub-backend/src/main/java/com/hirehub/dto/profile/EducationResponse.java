package com.hirehub.dto.profile;

public record EducationResponse(
        Long id,
        String institution,
        String degree,
        Integer startYear,
        Integer endYear
) {
}
