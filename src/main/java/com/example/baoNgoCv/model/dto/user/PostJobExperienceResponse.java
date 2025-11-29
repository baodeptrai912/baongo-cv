package com.example.baoNgoCv.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record PostJobExperienceResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("jobTitle") String jobTitle,
        @JsonProperty("companyName") String companyName,
        @JsonProperty("startDate") LocalDate startDate,
        @JsonProperty("endDate") LocalDate endDate,
        @JsonProperty("description") String description
) {
}
