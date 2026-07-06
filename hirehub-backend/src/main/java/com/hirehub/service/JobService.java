package com.hirehub.service;

import com.hirehub.dto.job.JobRequest;
import com.hirehub.dto.job.JobResponse;
import com.hirehub.dto.job.JobSearchRequest;
import com.hirehub.entity.Company;
import com.hirehub.entity.Job;
import com.hirehub.enums.JobStatus;
import com.hirehub.exception.ForbiddenException;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.mapper.JobMapper;
import com.hirehub.repository.JobRepository;
import com.hirehub.security.CurrentUser;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final CompanyService companyService;

    @Transactional
    public JobResponse create(JobRequest request) {
        Company company = companyService.findOwnedCompany(CurrentUser.get().getId());
        Job job = new Job();
        applyRequest(job, request);
        job.setCompany(company);
        Job savedJob = jobRepository.save(job);
        log.info("Job created with id {}", savedJob.getId());
        return JobMapper.toResponse(savedJob);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> search(JobSearchRequest request, Pageable pageable) {
        return jobRepository.findAll(specification(request), pageable).map(JobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> myJobs(Pageable pageable) {
        return jobRepository.findByCompanyOwnerUserIdAndStatusNot(CurrentUser.get().getId(), JobStatus.DELETED, pageable)
                .map(JobMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse get(Long jobId) {
        return JobMapper.toResponse(findActive(jobId));
    }

    @Transactional
    public JobResponse update(Long jobId, JobRequest request) {
        Job job = findActive(jobId);
        assertOwner(job);
        applyRequest(job, request);
        log.info("Job updated with id {}", job.getId());
        return JobMapper.toResponse(job);
    }

    @Transactional
    public void deleteOwned(Long jobId) {
        Job job = findActive(jobId);
        assertOwner(job);
        job.setStatus(JobStatus.DELETED);
    }

    @Transactional
    public void deleteByAdmin(Long jobId) {
        Job job = findActive(jobId);
        job.setStatus(JobStatus.DELETED);
    }

    public Job findOpenJob(Long jobId) {
        Job job = findActive(jobId);
        if (job.getStatus() != JobStatus.OPEN) {
            throw new ForbiddenException("Deleted or closed jobs cannot receive applications");
        }
        return job;
    }

    private Job findActive(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (job.getStatus() == JobStatus.DELETED) {
            throw new ResourceNotFoundException("Job not found");
        }
        return job;
    }

    private void applyRequest(Job job, JobRequest request) {
        job.setTitle(request.title().trim());
        job.setDescription(request.description().trim());
        job.setLocation(request.location().trim());
        job.setEmploymentType(request.employmentType());
        job.setExperienceLevel(request.experienceLevel());
        job.setSalary(request.salary());
        job.setDeadline(request.deadline());
    }

    private void assertOwner(Job job) {
        Long currentUserId = CurrentUser.get().getId();
        Long ownerUserId = job.getCompany().getOwner().getUser().getId();
        if (!ownerUserId.equals(currentUserId)) {
            throw new ForbiddenException("Recruiters can only manage their own jobs");
        }
    }

    private Specification<Job> specification(JobSearchRequest request) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("status"), JobStatus.OPEN));
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String like = "%" + request.keyword().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), like),
                        builder.like(builder.lower(root.get("description")), like)
                ));
            }
            if (request.location() != null && !request.location().isBlank()) {
                predicates.add(builder.equal(builder.lower(root.get("location")), request.location().toLowerCase()));
            }
            if (request.employmentType() != null) {
                predicates.add(builder.equal(root.get("employmentType"), request.employmentType()));
            }
            if (request.experienceLevel() != null) {
                predicates.add(builder.equal(root.get("experienceLevel"), request.experienceLevel()));
            }
            if (request.minSalary() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("salary"), request.minSalary()));
            }
            if (request.maxSalary() != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("salary"), request.maxSalary()));
            }
            if (request.company() != null && !request.company().isBlank()) {
                predicates.add(builder.like(builder.lower(root.get("company").get("name")), "%" + request.company().toLowerCase() + "%"));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
