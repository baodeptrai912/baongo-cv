package com.example.baoNgoCv.jpa.projection.jobPosting;

import com.example.baoNgoCv.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SavedJobProjection {
    LocalDateTime getSavedAt();
    Long getId();
    String getTitle();
    LocationType getLocation();
    JobType getJobType();
    SalaryRange getSalaryRange();
    LocalDate getPostedDate();
    LocalDate getApplicationDeadline();
    JobPostingStatus getStatus();
    ExperienceLevel getExperience();
    IndustryType getIndustry();
    Integer getMaxApplicants();
    Long getCompanyId();
    String getCompanyName();
    String getCompanyLogo();
    Boolean getHasApplied();
    ApplicationStatus getApplicationStatus();
    LocalDateTime getApplicationDate();
    Long getCurrentApplicants();
}
