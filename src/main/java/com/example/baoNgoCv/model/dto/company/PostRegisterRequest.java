package com.example.baoNgoCv.model.dto.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostRegisterRequest(
        @NotEmpty(message = "Company name is required")
        String name,

        @NotEmpty(message = "Username is required")
        @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
        String username,

        @NotEmpty(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        String confirmPassword,

        @NotEmpty(message = "Business email is required")
        @Pattern(
                regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$",
                message = "Invalid email format "
        )
        String contactEmail,

        @NotEmpty(message = "Headquarters location is required")
        String location
) {
    // Định nghĩa Regex chuẩn cho Email (Ví dụ này là chuẩn OWASP Validation)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    @JsonIgnore
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }


}