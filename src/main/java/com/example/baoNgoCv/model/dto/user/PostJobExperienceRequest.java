package com.example.baoNgoCv.model.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
public record PostJobExperienceRequest(
        @NotBlank(message = "Tên công việc không được để trống")
        @JsonProperty("jobTitle")
        String jobTitle,

        @NotBlank(message = "Tên công ty không được để trống")
        @JsonProperty("companyName")
        String companyName,

        @NotNull(message = "Ngày bắt đầu không được để trống")
        @PastOrPresent(message = "Ngày bắt đầu không thể trong tương lai")
        @JsonProperty("startDate")
        LocalDate startDate,

        @JsonProperty("endDate")
        LocalDate endDate,

        @JsonProperty("description")
        String description
) {
    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    public boolean isEndDateAfterStartDate() {
        if (endDate == null || startDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }
}
