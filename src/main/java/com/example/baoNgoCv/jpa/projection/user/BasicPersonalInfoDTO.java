package com.example.baoNgoCv.jpa.projection.user;

import lombok.Builder;

import java.time.LocalDate;
import java.time.Period;

@Builder
public record BasicPersonalInfoDTO(
        String fullName,
        String email,
        String phoneNumber,
        String address,
        LocalDate dateOfBirth,
        String gender,
        String nationality
) {

    public Integer getAge() {
        return dateOfBirth != null ?
                Period.between(dateOfBirth, LocalDate.now()).getYears() : null;
    }

    public boolean isComplete() {
        return fullName != null && !fullName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                address != null && !address.trim().isEmpty() &&
                dateOfBirth != null && gender != null && nationality != null;
    }

    public int getCompletionPercentage() {
        int total = 7;
        int completed = 0;

        if (fullName != null && !fullName.trim().isEmpty()) completed++;
        if (email != null && !email.trim().isEmpty()) completed++;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) completed++;
        if (address != null && !address.trim().isEmpty()) completed++;
        if (dateOfBirth != null) completed++;
        if (gender != null) completed++;
        if (nationality != null) completed++;

        return (completed * 100) / total;
    }

    public String getCompletionStatus() {
        int percentage = getCompletionPercentage();
        if (percentage == 100) return "Complete";
        if (percentage >= 80) return "Almost Complete";
        if (percentage >= 60) return "Good Progress";
        return "Needs Attention";
    }
}
