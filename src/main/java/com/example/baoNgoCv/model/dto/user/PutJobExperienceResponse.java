package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.entity.JobExperience;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record PutJobExperienceResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("jobTitle") String jobTitle,
        @JsonProperty("companyName") String companyName,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("endDate") LocalDate endDate,
        @JsonProperty("description") String description,
        @JsonProperty("isCurrentJob") Boolean isCurrentJob
) {

    public static PutJobExperienceResponse from(JobExperience jobExperience) {
        return new PutJobExperienceResponse(
                jobExperience.getId(),
                jobExperience.getJobTitle(),
                jobExperience.getCompanyName(),
                jobExperience.getStartDate(),
                jobExperience.getEndDate(),
                jobExperience.getDescription(),
                jobExperience.getEndDate() == null
        );
    }
}
