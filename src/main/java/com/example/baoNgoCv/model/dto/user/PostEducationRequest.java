package com.example.baoNgoCv.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PostEducationRequest(

        @JsonProperty("educationId")
        Long educationId, // Optional - dùng khi update

        @NotBlank(message = "Degree is required")
        @Size(max = 255, message = "Degree must not exceed 255 characters")
        @JsonProperty("degree")
        String degree,

        @NotBlank(message = "Institution is required")
        @Size(max = 255, message = "Institution must not exceed 255 characters")
        @JsonProperty("institution")
        String institution,

        @NotNull(message = "Start date is required")
        @PastOrPresent(message = "Start date cannot be in the future")
        @JsonProperty("educationStartDate")
        LocalDate startDate,

        @JsonProperty("educationEndDate")
        LocalDate endDate,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        @JsonProperty("educationDetail")
        String notes

) {

    @AssertTrue(message = "End date must be after or equal to start date")
    public boolean isValidDateRange() {

        if (startDate == null || endDate == null) {
            return true;
        }

        return !endDate.isBefore(startDate);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
