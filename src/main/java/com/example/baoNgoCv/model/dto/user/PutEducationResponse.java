package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.entity.Education;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PutEducationResponse(
    @JsonProperty("message")
    String message,

    @JsonProperty("id")
    String id,

    @JsonProperty("degree")
    String degree,

    @JsonProperty("institution")
    String institution,

    @JsonProperty("startDate")
    String startDate,

    @JsonProperty("endDate")
    String endDate,

    @JsonProperty("notes")
    String notes
) {

        public static PutEducationResponse fromEducation(Education education, String message) {
            return new PutEducationResponse(
                    message,
                    String.valueOf(education.getId()),
                    education.getDegree(),
                    education.getInstitution(),
                    education.getStartDate().toString(),
                    education.getEndDate() != null ? education.getEndDate().toString() : null,
                    education.getNotes() != null ? education.getNotes() : ""
            );
        }
    }
