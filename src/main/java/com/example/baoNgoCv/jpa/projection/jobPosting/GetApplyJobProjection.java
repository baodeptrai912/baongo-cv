package com.example.baoNgoCv.jpa.projection.jobPosting;

import com.example.baoNgoCv.model.enums.JobPostingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface GetApplyJobProjection {

    Long getJobId();
    String getJobTitle();
    String getJobLocation();
    String getJobSalaryRange();
    String getJobExperience();
    String getJobStatus();
    LocalDate getJobApplicationDeadline();
    Integer getJobMaxApplicants();
    LocalDate getJobCreatedAt();

    Long getCompanyId();
    String getCompanyName();
    String getCompanyLogo();
    Long getJobApplicantCount();

    Long getUserId();
    String getUserFullName();
    String getUserEmail();
    String getUserPhoneNumber();

    Long getApplicantId();
    String getApplicantStatus();
    LocalDateTime getApplicantAppliedAt();
    LocalDateTime getApplicantDeletedAt();

    Long getJobSavedId();

    default String getStatusDisplay() {
        return getJobStatus() != null ?
                JobPostingStatus.valueOf(getJobStatus()).name() : "Unknown";
    }

    default String getApplicationDeadlineFormatted() {
        return getJobApplicationDeadline() != null ?
                getJobApplicationDeadline().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
    }

    default String getCreatedAtFormatted() {
        return getJobCreatedAt() != null ?
                getJobCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "N/A";
    }

    default Boolean getIsOpen() {
        return "OPEN".equals(getJobStatus());
    }

    default Boolean getCanAcceptMoreApplicants() {
        return getJobApplicantCount() != null && getJobMaxApplicants() != null &&
                getJobApplicantCount() < getJobMaxApplicants();
    }

    default Boolean getAlreadyApplied() {
        return getApplicantId() != null && getApplicantDeletedAt() == null;
    }

    default Boolean getIsDeleted() {
        return getApplicantId() != null && getApplicantDeletedAt() != null;
    }

    default Boolean getIsJobSaved() {
        return getJobSavedId() != null;
    }

    default Boolean getUserHasCompleteProfile() {
        return getUserFullName() != null && getUserEmail() != null && getUserPhoneNumber() != null;
    }
}

