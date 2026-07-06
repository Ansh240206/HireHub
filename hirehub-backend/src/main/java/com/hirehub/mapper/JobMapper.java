package com.hirehub.mapper;

import com.hirehub.dto.job.JobResponse;
import com.hirehub.entity.Job;

public final class JobMapper {

    private JobMapper() {
    }

    public static JobResponse toResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getExperienceLevel(),
                job.getSalary(),
                job.getDeadline(),
                job.getStatus(),
                job.getCompany().getId(),
                job.getCompany().getName()
        );
    }
}
