package com.hirehub.controller;

import com.hirehub.dto.common.ApiResponse;
import com.hirehub.dto.user.UserResponse;
import com.hirehub.enums.RoleName;
import com.hirehub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(@RequestParam(required = false) RoleName role, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Users fetched", userService.listUsers(role, pageable)));
    }

    @PatchMapping("/{userId}/enable")
    public ResponseEntity<ApiResponse<UserResponse>> enableUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User enabled", userService.setEnabled(userId, true)));
    }

    @PatchMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse<UserResponse>> disableUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "User disabled", userService.setEnabled(userId, false)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
