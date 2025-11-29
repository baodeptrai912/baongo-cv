package com.example.baoNgoCv.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PutEducationRequest(
        @NotBlank(message = "Degree là bắt buộc")
        @JsonProperty("degree")
        String degree,

        @NotBlank(message = "Institution là bắt buộc")
        @JsonProperty("institution")
        String institution,

        @NotNull(message = "Start date là bắt buộc")
        @JsonProperty("startDate")
        LocalDate startDate,


        @JsonProperty("endDate")
        LocalDate endDate,

        @JsonProperty("notes")
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
