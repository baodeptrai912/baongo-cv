package com.example.baoNgoCv.jpa.projection.jobPosting;

import com.example.baoNgoCv.model.enums.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
public interface JobCardProjection {

    Long getId();
    String getTitle();
    LocationType getLocation();
    JobType getJobType();
    SalaryRange getSalaryRange();
    ExperienceLevel getExperience();
    IndustryType getIndustry();
    LocalDate getPostedDate();
    LocalDate getApplicationDeadline();
    Integer getMaxApplicants();

    String getCompanyName();
    String getCompanyLogo();
    Integer getApplicantCount();
    Integer getViewCount();

    Integer getTrending();

    String getRequirementsString();

    default List<String> getTopRequirements() {
        String reqString = getRequirementsString();
        if (reqString == null || reqString.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(reqString.split("\\|\\|\\|"))
                .limit(3)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
