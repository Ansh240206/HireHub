package com.hirehub.repository;

import com.hirehub.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    boolean existsByApplicantProfileIdAndJobId(Long applicantProfileId, Long jobId);

    void deleteByApplicantProfileIdAndJobId(Long applicantProfileId, Long jobId);

    Page<SavedJob> findByApplicantProfileUserId(Long userId, Pageable pageable);
}
