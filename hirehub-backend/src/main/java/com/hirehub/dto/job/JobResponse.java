package com.hirehub.dto.job;

import com.hirehub.enums.EmploymentType;
import com.hirehub.enums.ExperienceLevel;
import com.hirehub.enums.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobResponse(
        Long id,
        String title,
        String description,
        String location,
        EmploymentType employmentType,
        ExperienceLevel experienceLevel,
        BigDecimal salary,
        LocalDate deadline,
        JobStatus status,
        Long companyId,
        String companyName
) {
}
