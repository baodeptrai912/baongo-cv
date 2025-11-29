package com.example.baoNgoCv.jpa.projection.user;

import java.time.LocalDate;

public record EducationDTO(
        Long id,
        String degree,
        String institution,
        LocalDate startDate,
        LocalDate endDate,
        String notes,
        Boolean graduated
) {
    public Boolean isCurrentEducation() {
        return endDate == null && !graduated;
    }

    public String getFullDegree() {
        return degree + " in General Studies";
    }
}
