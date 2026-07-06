package com.hirehub.repository;

import com.hirehub.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByApplicantProfileIdAndJobId(Long applicantProfileId, Long jobId);

    Page<Application> findByApplicantProfileUserId(Long userId, Pageable pageable);

    Page<Application> findByJobCompanyOwnerUserId(Long userId, Pageable pageable);
}
