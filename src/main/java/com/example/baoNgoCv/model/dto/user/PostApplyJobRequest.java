package com.example.baoNgoCv.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PostApplyJobRequest(
        @NotNull(message = "CV file is required")
        MultipartFile cvUpload,

        @NotBlank(message = "Cover letter is required")
        @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
        String coverLetter
) {}
