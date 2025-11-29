package com.example.baoNgoCv.jpa.projection.jobPosting;

import com.example.baoNgoCv.model.enums.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public interface JobPostingForApplyJobProjection {

    Long getId();
    String getTitle();
    String getDescriptions();
    LocationType getLocation();
    JobType getJobType();
    SalaryRange getSalaryRange();
    LocalDate getPostedDate();
    LocalDate getApplicationDeadline();
    JobPostingStatus getStatus();
    ExperienceLevel getExperience();
    IndustryType getIndustry();

    String getRequirements();
    String getBenefits();

    Integer getTargetHires();
    Integer getReceivedCount();

    // Default: trả về List<String>
    default List<String> getDescriptionsList() {
        String str = getDescriptions();
        return (str != null && !str.isEmpty())
                ? Arrays.asList(str.split("\\|\\|\\|"))
                : List.of();
    }

    default List<String> getRequirementsList() {
        String str = getRequirements();
        return (str != null && !str.isEmpty())
                ? Arrays.asList(str.split("\\|\\|\\|"))
                : List.of();
    }

    default List<String> getBenefitsList() {
        String str = getBenefits();
        return (str != null && !str.isEmpty())
                ? Arrays.asList(str.split("\\|\\|\\|"))
                : List.of();
    }

    // Các derived attributes khác

    default String getApplicationDeadlineFormatted() {
        return getApplicationDeadline() != null
                ? getApplicationDeadline().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "No deadline";
    }

    default String getCreatedAtFormatted() {
        return getPostedDate() != null
                ? getPostedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                : "Unknown";
    }

    default String getStatusDisplay() {
        return getStatus() != null ? getStatus().name() : "UNKNOWN";
    }

    default boolean isOpen() {
        return getStatus() == JobPostingStatus.OPEN
                && getApplicationDeadline() != null
                && getApplicationDeadline().isAfter(LocalDate.now());
    }

    default boolean hasDescriptions() {
        return !getDescriptionsList().isEmpty();
    }

    default boolean hasRequirements() {
        return !getRequirementsList().isEmpty();
    }

    default boolean hasBenefits() {
        return !getBenefitsList().isEmpty();
    }

    default boolean canShowApplicationForm(
            UserForApplyJobProjection user,
            boolean hasApplied,
            boolean isExpired
    ) {

        return getStatus() == JobPostingStatus.OPEN
                && !hasApplied
                && !isExpired;
    }

    default String getPrimaryMessage(boolean hasApplied, boolean isExpired) {
        if (hasApplied) {
            return "You have already applied for this job.";
        }
        if (isExpired) {
            return "Application deadline has passed.";
        }
        if (getStatus() != JobPostingStatus.OPEN) {
            return "This job is closed or no longer accepting applications.";
        }
        return null;
    }

    default String getPrimaryMessageType(boolean hasApplied, boolean isExpired) {
        if (hasApplied) return "info";
        if (isExpired || getStatus() != JobPostingStatus.OPEN) return "warning";
        return "success";
    }

    default Integer getSafeReceivedCount() {
        return getReceivedCount() != null ? getReceivedCount() : 0;
    }

    default Integer getSafeTargetHires() {
        // Nếu targetHires null hoặc <= 0 thì trả về 1 để tránh lỗi chia cho 0
        return (getTargetHires() != null && getTargetHires() > 0) ? getTargetHires() : 1;
    }

}
