package com.hirehub.controller;

import com.hirehub.dto.application.ApplicationResponse;
import com.hirehub.dto.application.ApplicationStatusUpdateRequest;
import com.hirehub.dto.common.ApiResponse;
import com.hirehub.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/jobs/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_APPLICANT')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(@PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Application submitted", applicationService.apply(jobId)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_APPLICANT')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> myApplications(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Applications fetched", applicationService.myApplications(pageable)));
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> applicationsForOwnedJobs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Applicants fetched", applicationService.applicationsForOwnedJobs(pageable)));
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Application status updated", applicationService.updateStatus(applicationId, request)));
    }
}
