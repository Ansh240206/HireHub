package com.hirehub.mapper;

import com.hirehub.dto.application.ApplicationResponse;
import com.hirehub.entity.Application;

public final class ApplicationMapper {

    private ApplicationMapper() {
    }

    public static ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany().getName(),
                application.getApplicantProfile().getUser().getId(),
                application.getApplicantProfile().getUser().getName(),
                application.getStatus()
        );
    }
}
