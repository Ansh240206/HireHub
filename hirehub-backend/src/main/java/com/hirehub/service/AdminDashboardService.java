package com.hirehub.service;

import com.hirehub.dto.admin.AdminDashboardResponse;
import com.hirehub.enums.RoleName;
import com.hirehub.repository.ApplicationRepository;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.JobRepository;
import com.hirehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.findByRole(RoleName.ROLE_APPLICANT, org.springframework.data.domain.Pageable.unpaged()).getTotalElements(),
                userRepository.findByRole(RoleName.ROLE_RECRUITER, org.springframework.data.domain.Pageable.unpaged()).getTotalElements(),
                companyRepository.count(),
                jobRepository.count(),
                applicationRepository.count()
        );
    }
}
