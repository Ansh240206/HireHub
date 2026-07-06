package com.hirehub.service;

import com.hirehub.dto.job.JobResponse;
import com.hirehub.entity.ApplicantProfile;
import com.hirehub.entity.Job;
import com.hirehub.entity.SavedJob;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.mapper.JobMapper;
import com.hirehub.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final ApplicantProfileService applicantProfileService;
    private final JobService jobService;

    @Transactional
    public JobResponse saveJob(Long jobId) {
        ApplicantProfile profile = applicantProfileService.findOwnProfile();
        Job job = jobService.findOpenJob(jobId);
        if (savedJobRepository.existsByApplicantProfileIdAndJobId(profile.getId(), job.getId())) {
            throw new DuplicateResourceException("Job is already saved");
        }
        SavedJob savedJob = new SavedJob();
        savedJob.setApplicantProfile(profile);
        savedJob.setJob(job);
        savedJobRepository.save(savedJob);
        return JobMapper.toResponse(job);
    }

    @Transactional
    public void removeSavedJob(Long jobId) {
        ApplicantProfile profile = applicantProfileService.findOwnProfile();
        savedJobRepository.deleteByApplicantProfileIdAndJobId(profile.getId(), jobId);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> listSavedJobs(Pageable pageable) {
        return savedJobRepository.findByApplicantProfileUserId(com.hirehub.security.CurrentUser.get().getId(), pageable)
                .map(savedJob -> JobMapper.toResponse(savedJob.getJob()));
    }
}
