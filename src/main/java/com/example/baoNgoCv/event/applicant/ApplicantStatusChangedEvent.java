package com.example.baoNgoCv.event.applicant;

import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.enums.ApplicationStatus;

public record ApplicantStatusChangedEvent(
        // --- A. Định danh (Who) ---
        Long applicantId,
        Long userId,
        String targetUsername,
        String userEmail,
        String userFullName,

        // --- B. Ngữ cảnh (Context) ---
        String jobTitle,
        String companyName,
        String companyLogo,

        // --- C. Trạng thái mới (Target) ---
        ApplicationStatus newStatus
) {

    public static ApplicantStatusChangedEvent from(Applicant applicant, ApplicationStatus newStatus) {
        return new ApplicantStatusChangedEvent(
                applicant.getId(),
                applicant.getUser().getId(),
                applicant.getUser().getUsername(),

                // Null-safe cho Email và Fullname (phòng trường hợp ContactInfo chưa có)
                (applicant.getUser().getContactInfo() != null) ? applicant.getUser().getContactInfo().getEmail() : applicant.getUser().getContactInfo().getEmail(),
                (applicant.getUser().getPersonalInfo() != null) ? applicant.getUser().getPersonalInfo().getFullName() : "Candidate",

                applicant.getJobPosting().getTitle(),
                applicant.getJobPosting().getCompany().getName(),
                applicant.getJobPosting().getCompany().getCompanyLogo(),

                newStatus
        );
    }
}