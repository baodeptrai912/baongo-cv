package com.example.baoNgoCv.service.validationService;

import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.domainService.CompanyService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueUsernameValidator implements ConstraintValidator<ValidUniqueUsername, String> {

    private final UserService userService;
    private final CompanyService companyService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        // Let @NotBlank handle null or empty strings.
        // This validator's only responsibility is to check for uniqueness.
        if (username == null || username.isBlank()) {
            return true;
        }
        // Return false if username exists in either users or companies table
        return !userService.existsByUsername(username)
                && companyService.findByUserName(username).isEmpty();
    }
}
