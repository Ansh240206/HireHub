package com.hirehub.controller;

import com.hirehub.dto.common.ApiResponse;
import com.hirehub.dto.company.CompanyRequest;
import com.hirehub.dto.company.CompanyResponse;
import com.hirehub.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> list(@RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Companies fetched", companyService.list(keyword, pageable)));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> get(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Company fetched", companyService.get(companyId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Company created", companyService.create(request)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateOwned(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Company updated", companyService.updateOwnedCompany(request)));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_RECRUITER')")
    public ResponseEntity<Void> deleteOwned() {
        companyService.deleteOwnedCompany();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteByAdmin(@PathVariable Long companyId) {
        companyService.deleteByAdmin(companyId);
        return ResponseEntity.noContent().build();
    }
}
