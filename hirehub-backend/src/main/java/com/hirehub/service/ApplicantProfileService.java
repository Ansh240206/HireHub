package com.hirehub.service;

import com.hirehub.dto.profile.ApplicantProfileRequest;
import com.hirehub.dto.profile.ApplicantProfileResponse;
import com.hirehub.dto.profile.EducationRequest;
import com.hirehub.dto.profile.ExperienceRequest;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.Education;
import com.hirehub.entity.Experience;
import com.hirehub.entity.Skill;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.mapper.ApplicantProfileMapper;
import com.hirehub.repository.ApplicantProfileRepository;
import com.hirehub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicantProfileService {

    private final ApplicantProfileRepository applicantProfileRepository;

    @Transactional(readOnly = true)
    public ApplicantProfileResponse getOwnProfile() {
        return ApplicantProfileMapper.toResponse(findOwnProfile());
    }

    @Transactional
    public ApplicantProfileResponse updateOwnProfile(ApplicantProfileRequest request) {
        ApplicantProfile profile = findOwnProfile();
        profile.setPhone(request.phone().trim());
        profile.setLocation(request.location().trim());
        profile.setSummary(request.summary().trim());
        replaceEducation(profile, request.education());
        replaceExperience(profile, request.experience());
        replaceSkills(profile, request.skills());
        profile.setComplete(isComplete(profile));
        return ApplicantProfileMapper.toResponse(profile);
    }

    public ApplicantProfile findOwnProfile() {
        return applicantProfileRepository.findByUserId(CurrentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Applicant profile not found"));
    }

    private void replaceEducation(ApplicantProfile profile, List<EducationRequest> requests) {
        profile.getEducationEntries().clear();
        if (requests == null) {
            return;
        }
        requests.forEach(request -> {
            Education education = new Education();
            education.setApplicantProfile(profile);
            education.setInstitution(request.institution().trim());
            education.setDegree(request.degree().trim());
            education.setStartYear(request.startYear());
            education.setEndYear(request.endYear());
            profile.getEducationEntries().add(education);
        });
    }

    private void replaceExperience(ApplicantProfile profile, List<ExperienceRequest> requests) {
        profile.getExperienceEntries().clear();
        if (requests == null) {
            return;
        }
        requests.forEach(request -> {
            Experience experience = new Experience();
            experience.setApplicantProfile(profile);
            experience.setCompanyName(request.companyName().trim());
            experience.setRole(request.role().trim());
            experience.setDuration(request.duration());
            profile.getExperienceEntries().add(experience);
        });
    }

    private void replaceSkills(ApplicantProfile profile, List<String> skills) {
        profile.getSkills().clear();
        if (skills == null) {
            return;
        }
        skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(skillName -> {
                    Skill skill = new Skill();
                    skill.setApplicantProfile(profile);
                    skill.setName(skillName);
                    profile.getSkills().add(skill);
                });
    }

    private boolean isComplete(ApplicantProfile profile) {
        return hasText(profile.getPhone())
                && hasText(profile.getLocation())
                && hasText(profile.getSummary())
                && !profile.getEducationEntries().isEmpty()
                && !profile.getSkills().isEmpty();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
