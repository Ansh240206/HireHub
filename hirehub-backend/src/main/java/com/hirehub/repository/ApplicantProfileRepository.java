package com.hirehub.repository;

import com.hirehub.entity.ApplicantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicantProfileRepository extends JpaRepository<ApplicantProfile, Long> {

    Optional<ApplicantProfile> findByUserId(Long userId);
}
