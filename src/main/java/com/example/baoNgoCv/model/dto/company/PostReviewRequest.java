package com.example.baoNgoCv.model.dto.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PostReviewRequest(
        @NotNull(message = "Applicant ID is required")
        @JsonProperty("applicantId")
        Long applicantId,

        @NotNull(message = "Score is required")
        @Min(value = 1, message = "Score must be at least 1")
        @Max(value = 10, message = "Score must be at most 10")
        @JsonProperty("score")
        Double score,

        @JsonProperty("note")
        String note
) {}