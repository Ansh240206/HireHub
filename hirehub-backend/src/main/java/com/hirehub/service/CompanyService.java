package com.hirehub.service;

import com.hirehub.dto.company.CompanyRequest;
import com.hirehub.dto.company.CompanyResponse;
import com.hirehub.entity.Company;
import com.hirehub.entity.RecruiterProfile;
import com.hirehub.exception.DuplicateResourceException;
import com.hirehub.exception.ForbiddenException;
import com.hirehub.exception.ResourceNotFoundException;
import com.hirehub.mapper.CompanyMapper;
import com.hirehub.repository.CompanyRepository;
import com.hirehub.repository.RecruiterProfileRepository;
import com.hirehub.security.CurrentUser;
import com.hirehub.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;

    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        UserPrincipal principal = CurrentUser.get();
        if (companyRepository.findByOwnerUserId(principal.getId()).isPresent()) {
            throw new DuplicateResourceException("Recruiter already owns a company");
        }
        if (companyRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Company name already exists");
        }
        RecruiterProfile recruiterProfile = recruiterProfileRepository.findByUserId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
        Company company = new Company();
        company.setName(request.name().trim());
        company.setDescription(request.description());
        company.setWebsite(request.website());
        company.setLocation(request.location());
        company.setOwner(recruiterProfile);
        return CompanyMapper.toResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> list(String keyword, Pageable pageable) {
        Page<Company> companies = keyword == null || keyword.isBlank()
                ? companyRepository.findAll(pageable)
                : companyRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return companies.map(CompanyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(Long companyId) {
        return CompanyMapper.toResponse(find(companyId));
    }

    @Transactional
    public CompanyResponse updateOwnedCompany(CompanyRequest request) {
        Company company = companyRepository.findByOwnerUserId(CurrentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for recruiter"));
        company.setName(request.name().trim());
        company.setDescription(request.description());
        company.setWebsite(request.website());
        company.setLocation(request.location());
        return CompanyMapper.toResponse(company);
    }

    @Transactional
    public void deleteOwnedCompany() {
        Company company = companyRepository.findByOwnerUserId(CurrentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found for recruiter"));
        companyRepository.delete(company);
    }

    @Transactional
    public void deleteByAdmin(Long companyId) {
        Company company = find(companyId);
        companyRepository.delete(company);
    }

    public Company findOwnedCompany(Long userId) {
        return companyRepository.findByOwnerUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Recruiter must create a company before managing jobs"));
    }

    private Company find(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
    }
}
