package com.hirehub.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class ApplicantProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private User user;

    @Column(length = 1000)
    private String summary;

    private String phone;

    private String location;

    @Column(nullable = false)
    private boolean complete;

    @OneToOne(mappedBy = "applicantProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resume resume;

    @OneToMany(mappedBy = "applicantProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educationEntries = new ArrayList<>();

    @OneToMany(mappedBy = "applicantProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experienceEntries = new ArrayList<>();

    @OneToMany(mappedBy = "applicantProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Skill> skills = new ArrayList<>();
}
