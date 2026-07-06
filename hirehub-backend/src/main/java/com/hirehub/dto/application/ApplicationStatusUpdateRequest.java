package com.hirehub.dto.application;

import com.hirehub.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "Application status is required")
        ApplicationStatus status
) {
}
