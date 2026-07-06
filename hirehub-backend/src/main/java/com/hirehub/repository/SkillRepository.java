package com.hirehub.repository;

import com.hirehub.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByApplicantProfileUserId(Long userId);
}
