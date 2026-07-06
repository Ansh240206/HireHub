package com.hirehub.repository;

import com.hirehub.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByApplicantProfileUserId(Long userId);
}
