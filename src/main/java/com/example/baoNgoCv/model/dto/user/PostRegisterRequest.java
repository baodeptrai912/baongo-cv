package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.dto.common.PasswordConfirmation;
import com.example.baoNgoCv.service.validationService.PasswordMatches;
import com.example.baoNgoCv.service.validationService.ValidUniqueEmail;
import com.example.baoNgoCv.service.validationService.ValidUniqueUsername;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents the data transfer object (DTO) for a new user registration request.
 * This class encapsulates all the necessary information sent from the client.
 * It serves as a single source of truth for all validation rules related to user registration,
 * leveraging both standard and custom validation annotations.
 *
 * @see PasswordMatches for cross-field password validation.
 * @see PasswordConfirmation for the contract ensuring password fields are available.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class PostRegisterRequest implements PasswordConfirmation {

    /**
     * The desired username for the new account.
     * It is subject to several validation rules:
     * - Must not be blank.
     * - Must be between 3 and 50 characters long.
     * - Can only contain alphanumeric characters and underscores.
     * - Must be unique across both users and companies in the system (checked via @ValidUniqueUsername).
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    @ValidUniqueUsername
    private String username;

    /**
     * The desired password for the new account.
     * It must not be blank and must meet length requirements.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /**
     * The confirmation of the password. This field must match the 'password' field.
     * The matching logic is handled by the @PasswordMatches annotation at the class level.
     */
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    /**
     * The user's email address.
     * It is subject to several validation rules:
     * - Must not be blank.
     * - Must be a well-formed email address.
     * - Must not exceed 100 characters.
     * - Must be unique in the system (checked via @ValidUniqueEmail).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @ValidUniqueEmail
    private String email;
}
