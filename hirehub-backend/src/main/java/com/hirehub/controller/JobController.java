package com.hirehub.controller;

import com.hirehub.dto.common.ApiResponse;
import com.hirehub.dto.job.JobRequest;
import com.hirehub.dto.job.JobResponse;
import com.hirehub.dto.job.JobSearchRequest;
import com.hirehub.enums.EmploymentType;
import com.hirehub.enums.ExperienceLevel;
import com.hirehub.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) String company,
            Pageable pageable
    ) {
        JobSearchRequest request = new JobSearchRequest(keyword, location, employmentType, experienceLevel, minSalary, maxSalary, company);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Jobs fetched", jobService.search(request, pageable)));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> get(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Job fetched", jobService.get(jobId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> myJobs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Recruiter jobs fetched", jobService.myJobs(pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Job created", jobService.create(request)));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> update(@PathVariable Long jobId, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Job updated", jobService.update(jobId, request)));
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<Void> deleteOwned(@PathVariable Long jobId) {
        jobService.deleteOwned(jobId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{jobId}/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long jobId) {
        jobService.deleteByAdmin(jobId);
        return ResponseEntity.noContent().build();
    }
}
