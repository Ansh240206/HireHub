package com.hirehub.dto.profile;

import java.util.List;

public record ApplicantProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        String location,
        String summary,
        boolean complete,
        ResumeResponse resume,
        List<EducationResponse> education,
        List<ExperienceResponse> experience,
        List<String> skills
) {
}
