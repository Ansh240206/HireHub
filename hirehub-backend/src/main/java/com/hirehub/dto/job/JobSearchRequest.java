package com.hirehub.dto.job;

import com.hirehub.enums.EmploymentType;
import com.hirehub.enums.ExperienceLevel;

import java.math.BigDecimal;

public record JobSearchRequest(
        String keyword,
        String location,
        EmploymentType employmentType,
        ExperienceLevel experienceLevel,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String company
) {
}
