package com.hirehub.mapper;

import com.hirehub.dto.company.CompanyResponse;
import com.hirehub.entity.Company;

public final class CompanyMapper {

    private CompanyMapper() {
    }

    public static CompanyResponse toResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getDescription(),
                company.getWebsite(),
                company.getLocation(),
                company.getOwner().getUser().getId(),
                company.getOwner().getUser().getName()
        );
    }
}
