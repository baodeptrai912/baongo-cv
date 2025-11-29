package com.example.baoNgoCv.jpa.projection.jobPosting;

import com.example.baoNgoCv.model.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface JobDetailWithUserStatusProjection {

    Long getId();
    String getTitle();

    LocationType getLocation();
    JobType getJobType();
    SalaryRange getSalaryRange();
    ExperienceLevel getExperience();
    IndustryType getIndustry();
    JobPostingStatus getStatus();

    LocalDate getApplicationDeadline();
    Integer getMaxApplicants();
    LocalDate getPostedDate();

    Long getCompanyId();
    String getCompanyName();
    String getCompanyLogo();

    Long getApplicantCount();

    Long getApplicationId();
    String getApplicationStatus();
    LocalDateTime getApplicationDeletedAt();

    Boolean getJobSaved();
}
