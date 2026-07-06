package com.hirehub.dto.admin;

public record AdminDashboardResponse(
        long users,
        long applicants,
        long recruiters,
        long companies,
        long jobs,
        long applications
) {
}
