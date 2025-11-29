package com.example.baoNgoCv.model.dto.common;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank(message = "Current password is required.")
    String currentPassword,

    @NotBlank(message = "New password is required.")
    @Size(min = 6, message = "New password must be at least 6 characters long.")
    String newPassword,

    @NotBlank(message = "Please confirm your new password.")
    String confirmedNewPassword
) {
    @AssertTrue(message = "New password and confirm password must match")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmedNewPassword);
    }
}