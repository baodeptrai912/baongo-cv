package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_settings")
public class CompanySetting {

    @Id
    private Long id;

    @Column(name = "email_on_new_applicant", nullable = false)
    private boolean emailOnNewApplicant = true; // Giá trị mặc định là true

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "company_id")
    private Company company;

    // Constructors
    public CompanySetting() {
    }

    public CompanySetting(Company company, boolean emailOnNewApplicant) {
        this.company = company;
        this.id = company.getId();
        this.emailOnNewApplicant = emailOnNewApplicant;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEmailOnNewApplicant() {
        return emailOnNewApplicant;
    }

    public void setEmailOnNewApplicant(boolean emailOnNewApplicant) {
        this.emailOnNewApplicant = emailOnNewApplicant;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
        if (company != null) {
            this.id = company.getId();
        }
    }
}