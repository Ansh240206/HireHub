package com.hirehub.repository;

import com.hirehub.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByApplicantProfileId(Long applicantProfileId);
}
