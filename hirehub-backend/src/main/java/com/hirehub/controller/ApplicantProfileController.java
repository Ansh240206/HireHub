package com.hirehub.controller;

import com.hirehub.dto.common.ApiResponse;
import com.hirehub.dto.profile.ApplicantProfileRequest;
import com.hirehub.dto.profile.ApplicantProfileResponse;
import com.hirehub.dto.profile.ResumeResponse;
import com.hirehub.entity.Resume;
import com.hirehub.service.ApplicantProfileService;
import com.hirehub.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/applicant-profile")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_APPLICANT')")
public class ApplicantProfileController {

    private final ApplicantProfileService applicantProfileService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ApplicantProfileResponse>> getOwnProfile() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Applicant profile fetched", applicantProfileService.getOwnProfile()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ApplicantProfileResponse>> updateOwnProfile(@Valid @RequestBody ApplicantProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Applicant profile updated", applicantProfileService.updateOwnProfile(request)));
    }

    @PostMapping("/me/resume")
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(@RequestParam("file") MultipartFile file) {
        Resume resume = fileStorageService.uploadResume(file);
        ResumeResponse response = new ResumeResponse(resume.getId(), resume.getOriginalFileName(), resume.getFilePath(), resume.getSizeBytes());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Resume uploaded", response));
    }
}
