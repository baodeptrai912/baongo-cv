package com.example.baoNgoCv.model.dto.applicant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostScheduleInterviewRequest(
        // ID của hồ sơ ứng viên (Applicant)
        @NotNull(message = "Applicant ID must not be null.")
        @Min(value = 1, message = "Applicant ID must be positive.")
        Long applicantId,

        // Tiêu đề email
        @NotBlank(message = "Email subject must not be empty.")
        @Size(min = 5, max = 255, message = "Subject must be between 5 and 255 characters.")
        String subject,

        // Nội dung email (đã gộp Date, Time, Location)
        @NotBlank(message = "Email content must not be empty.")
        @Size(min = 20, message = "Content must be at least 20 characters long.")
        String content
) {

}