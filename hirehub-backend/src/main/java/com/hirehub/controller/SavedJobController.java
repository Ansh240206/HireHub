package com.hirehub.controller;

import com.hirehub.dto.common.ApiResponse;
import com.hirehub.dto.job.JobResponse;
import com.hirehub.service.SavedJobService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_APPLICANT')")
public class SavedJobController {

    private final SavedJobService savedJobService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Saved jobs fetched", savedJobService.listSavedJobs(pageable)));
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> save(@PathVariable Long jobId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Job saved", savedJobService.saveJob(jobId)));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> remove(@PathVariable Long jobId) {
        savedJobService.removeSavedJob(jobId);
        return ResponseEntity.noContent().build();
    }
}
