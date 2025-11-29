package com.example.baoNgoCv.jpa.projection.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Record cho basic profile data (không có associations)
public record BasicProfileResponse(
        Long id,
        String username,
        String profilePicture,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String nationality,
        String email,
        String phoneNumber,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isProfilePublic
) {

    public Integer getAge() {
        return dateOfBirth != null ?
                java.time.Period.between(dateOfBirth, LocalDate.now()).getYears()
                : null;
    }

    public String getMemberSince() {
        return createdAt != null ?
                createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"))
                : null;
    }

    public Boolean hasCompleteProfile() {
        return fullName != null && dateOfBirth != null && gender != null
                && nationality != null && email != null
                && phoneNumber != null && address != null;
    }
}

