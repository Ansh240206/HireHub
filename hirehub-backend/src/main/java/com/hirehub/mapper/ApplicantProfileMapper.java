package com.hirehub.mapper;

import com.hirehub.dto.profile.ApplicantProfileResponse;
import com.hirehub.dto.profile.EducationResponse;
import com.hirehub.dto.profile.ExperienceResponse;
import com.hirehub.dto.profile.ResumeResponse;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.Education;
import com.hirehub.entity.Experience;
import com.hirehub.entity.Resume;
import com.hirehub.entity.Skill;

import java.util.List;

public final class ApplicantProfileMapper {

    private ApplicantProfileMapper() {
    }

    public static ApplicantProfileResponse toResponse(ApplicantProfile profile) {
        Resume resume = profile.getResume();
        ResumeResponse resumeResponse = resume == null ? null : new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getFilePath(),
                resume.getSizeBytes()
        );
        return new ApplicantProfileResponse(
                profile.getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getPhone(),
                profile.getLocation(),
                profile.getSummary(),
                profile.isComplete(),
                resumeResponse,
                mapEducation(profile.getEducationEntries()),
                mapExperience(profile.getExperienceEntries()),
                profile.getSkills().stream().map(Skill::getName).toList()
        );
    }

    private static List<EducationResponse> mapEducation(List<Education> education) {
        return education.stream()
                .map(item -> new EducationResponse(item.getId(), item.getInstitution(), item.getDegree(), item.getStartYear(), item.getEndYear()))
                .toList();
    }

    private static List<ExperienceResponse> mapExperience(List<Experience> experience) {
        return experience.stream()
                .map(item -> new ExperienceResponse(item.getId(), item.getCompanyName(), item.getRole(), item.getDuration()))
                .toList();
    }
}
