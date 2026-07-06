package com.hirehub.controller;

import com.hirehub.dto.admin.AdminDashboardResponse;
import com.hirehub.dto.common.ApiResponse;
import com.hirehub.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Admin dashboard fetched", adminDashboardService.dashboard()));
    }
}
