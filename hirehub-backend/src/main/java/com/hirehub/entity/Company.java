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
public class Company extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 2000)
    private String description;

    private String website;

    private String location;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, unique = true)
    private RecruiterProfile owner;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Job> jobs = new ArrayList<>();
}
