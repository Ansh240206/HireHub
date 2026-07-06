package com.hirehub.dto.application;

import com.hirehub.enums.ApplicationStatus;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        Long applicantUserId,
        String applicantName,
        ApplicationStatus status
) {
}
