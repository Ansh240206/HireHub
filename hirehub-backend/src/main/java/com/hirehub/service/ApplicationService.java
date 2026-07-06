package com.hirehub.service;

import com.hirehub.dto.application.ApplicationResponse;
import com.hirehub.dto.application.ApplicationStatusUpdateRequest;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.Application;
import com.hirehub.entity.Job;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.exception.ForbiddenException;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.mapper.ApplicationMapper;
import com.hirehub.repository.ApplicationRepository;
import com.hirehub.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicantProfileService applicantProfileService;
    private final JobService jobService;

    @Transactional
    public ApplicationResponse apply(Long jobId) {
        ApplicantProfile profile = applicantProfileService.findOwnProfile();
        if (!profile.isComplete()) {
            throw new ForbiddenException("Complete applicant profile before applying");
        }
        Job job = jobService.findOpenJob(jobId);
        if (applicationRepository.existsByApplicantProfileIdAndJobId(profile.getId(), job.getId())) {
            throw new DuplicateResourceException("Applicant has already applied to this job");
        }
        Application application = new Application();
        application.setApplicantProfile(profile);
        application.setJob(job);
        Application savedApplication = applicationRepository.save(application);
        log.info("Application submitted with id {}", savedApplication.getId());
        return ApplicationMapper.toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> myApplications(Pageable pageable) {
        return applicationRepository.findByApplicantProfileUserId(CurrentUser.get().getId(), pageable)
                .map(ApplicationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> applicationsForOwnedJobs(Pageable pageable) {
        return applicationRepository.findByJobCompanyOwnerUserId(CurrentUser.get().getId(), pageable)
                .map(ApplicationMapper::toResponse);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatusUpdateRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        Long ownerUserId = application.getJob().getCompany().getOwner().getUser().getId();
        if (!ownerUserId.equals(CurrentUser.get().getId())) {
            throw new ForbiddenException("Recruiters can update status only for their own jobs");
        }
        application.setStatus(request.status());
        return ApplicationMapper.toResponse(application);
    }
}
