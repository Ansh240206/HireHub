package com.hirehub.repository;

import com.hirehub.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByApplicantProfileUserId(Long userId);
}
