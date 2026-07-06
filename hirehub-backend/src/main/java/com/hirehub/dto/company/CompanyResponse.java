package com.hirehub.dto.company;

public record CompanyResponse(
        Long id,
        String name,
        String description,
        String website,
        String location,
        Long ownerUserId,
        String ownerName
) {
}
