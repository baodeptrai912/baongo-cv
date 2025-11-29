package com.example.baoNgoCv.model.dto.common;

import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordRequest(
        @NotBlank(message = "Password is required.")
        String password
) {
}
