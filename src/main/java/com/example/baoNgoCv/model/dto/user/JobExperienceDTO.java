// Tạo file JobExperienceDTO.java
package com.example.baoNgoCv.model.dto.user;

import java.time.LocalDate;

public record JobExperienceDTO(
        Long id,
        String jobTitle,
        String companyName,
        LocalDate startDate,
        LocalDate endDate,
        String description
) {
    public Boolean isCurrentJob() {
        return endDate == null;
    }

    public String getDuration() {
        if (startDate == null) return "Unknown";

        LocalDate end = endDate != null ? endDate : LocalDate.now();
        long months = java.time.Period.between(startDate, end).toTotalMonths();

        return endDate != null ?
                months + " months" :
                months + " months (Current)";
    }
}
