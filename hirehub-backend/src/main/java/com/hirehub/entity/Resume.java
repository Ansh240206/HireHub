package com.hirehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Resume extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private ApplicantProfile applicantProfile;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private long sizeBytes;
}
