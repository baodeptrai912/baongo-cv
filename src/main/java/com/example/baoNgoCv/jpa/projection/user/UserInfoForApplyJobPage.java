package com.example.baoNgoCv.jpa.projection.user;


import lombok.Builder;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Builder
public record UserInfoForApplyJobPage(
        // ✅ Core identity
        Long id,

        // ✅ Essential info for job application (hiển thị trong user-info-card)
        String fullName,
        String email,
        String phoneNumber,

        // ✅ Extended profile info (cho profile completion check)
        String address,
        LocalDate dateOfBirth,
        String gender,
        String nationality,

        // ✅ Application-specific flags
        boolean hasUploadedCV,
        boolean emailVerified,
        boolean phoneVerified,

        // ✅ Profile completion tracking
        Integer profileCompletionPercentage
) {

    // ✅ Age calculation helper
    public Integer getAge() {
        return dateOfBirth != null ?
                Period.between(dateOfBirth, LocalDate.now()).getYears() : null;
    }

    // ✅ Core profile completion for job application
    public boolean hasMinimumRequiredInfo() {
        return fullName != null && !fullName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                emailVerified && phoneVerified;
    }


    // ✅ Application readiness check
    public boolean isReadyToApply() {
        return hasMinimumRequiredInfo() && hasUploadedCV;
    }

    // ✅ Dynamic completion percentage calculation
    public int calculateCompletionPercentage() {
        if (profileCompletionPercentage != null) {
            return profileCompletionPercentage;
        }

        int total = 9;
        int completed = 0;

        // Core fields (weight: 6/10)
        if (fullName != null && !fullName.trim().isEmpty()) completed++;
        if (email != null && !email.trim().isEmpty()) completed++;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) completed++;
        if (address != null && !address.trim().isEmpty()) completed++;
        if (dateOfBirth != null) completed++;
        if (gender != null && nationality != null) completed++;
        // Verification flags (weight: 2/10)
        if (emailVerified) completed++;
        if (phoneVerified) completed++;

        if (hasUploadedCV) completed++;

        return (completed * 100) / total;
    }

    // ✅ Status message for UI
    public String getCompletionStatus() {
        int percentage = calculateCompletionPercentage();
        if (percentage == 100) return "Complete Profile";
        if (percentage >= 80) return "Almost Complete";
        if (percentage >= 60) return "Good Progress";
        if (percentage >= 40) return "Basic Info Complete";
        return "Profile Needs Attention";
    }

    // ✅ Missing fields for UI guidance
    public List<String> getNextStepMessages() {
        List<String> messages = new ArrayList<>();

        if (!emailVerified) messages.add("Please verify your email address");
        if (!phoneVerified) messages.add("Please verify your phone number");
        if (fullName == null || fullName.trim().isEmpty()) messages.add("Please add your full name");
        if (!hasUploadedCV) messages.add("Please upload your CV");

        if (messages.isEmpty()) {
            messages.add("Profile looks great!");
        }

        return messages;
    }

    // ✅ Create with default values
    public static UserInfoForApplyJobPage createDefault(Long userId) {
        return UserInfoForApplyJobPage.builder()
                .id(userId)
                .hasUploadedCV(false)
                .emailVerified(false)
                .phoneVerified(false)
                .profileCompletionPercentage(0)
                .build();
    }

    // ✅ Validation for job application
    public boolean canApplyForJob() {
        return hasMinimumRequiredInfo() &&
                hasUploadedCV &&
                emailVerified;
    }

    // ✅ Get blocked reasons (for UI messaging)
    public String getApplicationBlockedReason() {
        if (!hasMinimumRequiredInfo()) {
            return "Please complete your basic profile information";
        }
        if (!emailVerified) {
            return "Please verify your email address before applying";
        }
        if (!hasUploadedCV) {
            return "Please upload your CV before applying";
        }
        return null; // No blocking issues
    }
}
