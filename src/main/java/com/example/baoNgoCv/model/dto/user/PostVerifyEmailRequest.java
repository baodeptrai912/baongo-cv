package com.example.baoNgoCv.model.dto.user;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostVerifyEmailRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email verification code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be exactly 6 digits")
    private String emailVerificationCode;
}