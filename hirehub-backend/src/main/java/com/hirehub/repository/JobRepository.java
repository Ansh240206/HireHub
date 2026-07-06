package com.hirehub.repository;

import com.hirehub.entity.Job;
import com.hirehub.enums.EmploymentType;
import com.hirehub.enums.ExperienceLevel;
import com.hirehub.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    Page<Job> findByCompanyOwnerUserIdAndStatusNot(Long userId, JobStatus status, Pageable pageable);

    Page<Job> findByCompanyNameContainingIgnoreCaseAndEmploymentTypeAndExperienceLevelAndStatus(
            String company,
            EmploymentType employmentType,
            ExperienceLevel experienceLevel,
            JobStatus status,
            Pageable pageable
    );
}
