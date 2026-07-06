package com.hirehub.dto.job;

import com.hirehub.enums.EmploymentType;
import com.hirehub.enums.ExperienceLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobRequest(
        @NotBlank(message = "Job title is mandatory")
        String title,

        @NotBlank(message = "Description is mandatory")
        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Employment type is required")
        EmploymentType employmentType,

        @NotNull(message = "Experience level is required")
        ExperienceLevel experienceLevel,

        @DecimalMin(value = "0.0", inclusive = true, message = "Salary cannot be negative")
        @NotNull(message = "Salary is required")
        BigDecimal salary,

        @FutureOrPresent(message = "Deadline cannot be in the past")
        @NotNull(message = "Deadline is required")
        LocalDate deadline
) {
}
