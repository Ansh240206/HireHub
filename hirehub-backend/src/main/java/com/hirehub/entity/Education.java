package com.hirehub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Education extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ApplicantProfile applicantProfile;

    @Column(nullable = false)
    private String institution;

    @Column(nullable = false)
    private String degree;

    private Integer startYear;

    private Integer endYear;
}
