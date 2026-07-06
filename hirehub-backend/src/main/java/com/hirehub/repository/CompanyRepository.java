package com.hirehub.repository;

import com.hirehub.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Company> findByOwnerUserId(Long userId);

    Page<Company> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
