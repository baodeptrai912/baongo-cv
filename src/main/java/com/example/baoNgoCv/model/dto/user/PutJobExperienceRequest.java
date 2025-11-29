package com.example.baoNgoCv.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
public record PutJobExperienceRequest(
        @JsonProperty("jobTitle")
        @NotBlank(message = "Job title is required")
        String jobTitle,

        @JsonProperty("companyName")
        @NotBlank(message = "Company name is required")
        String companyName,

        @JsonProperty("startDate")
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @JsonProperty("endDate")
        LocalDate endDate,

        @JsonProperty("description")
        @NotBlank(message = "Description is required")
        String description
) {

    @AssertTrue(message = "Start date cannot be after end date")
    public boolean isValidDateRange() {
        if (endDate == null) return true;
        return !startDate.isAfter(endDate);
    }
}
