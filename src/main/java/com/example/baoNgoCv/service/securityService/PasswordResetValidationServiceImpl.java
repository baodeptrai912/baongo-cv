package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.dto.user.GetForgetPasswordFinalResponse;

import com.example.baoNgoCv.model.dto.user.PostForgetPasswordFinalRequest;
import com.example.baoNgoCv.exception.securityException.*;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.service.domainService.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Enumeration;

/**
 * Implementation of PasswordResetValidationService for handling password reset validation logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetValidationServiceImpl implements PasswordResetValidationService {

    private final UserService userService;

    @Value("${app.password-reset.session-duration-minutes:5}")
    private int passwordResetSessionDurationMinutes;

    private static final String SESSION_VERIFIED_KEY = "forgetPasswordVerified_";
    private static final String SESSION_EXPIRY_KEY = "forgetPasswordExpiryTime_";

    @Override
    public GetForgetPasswordFinalResponse createPasswordResetPageData(String username, long remainingMs) {
        log.info("Creating password reset page data for user '{}' with {}ms remaining.", username, remainingMs);

        // Step 2: Create masked email for display.
        String originalEmail = "BaongoCV Sory!";
        String maskedEmail = maskEmail(originalEmail);
        log.debug("Masked email for user '{}' is '{}'.", username, maskedEmail);

        // Step 3: Build and return the response DTO.
        GetForgetPasswordFinalResponse response = GetForgetPasswordFinalResponse.full(username, remainingMs, maskedEmail);
        log.info("Successfully created page data for user '{}'.", username);

        return response;
    }



    /**
     * Private method for boolean validation (all-in-one check)
     */
    private boolean canAccessPasswordResetPage(String username, HttpSession session) {
        // 1. Validate username parameter
        if (username == null || username.trim().isEmpty()) {
            log.warn("Empty username parameter in password reset validation");
            return false;
        }

        // 4. Check session verification
        Boolean isVerified = (Boolean) session.getAttribute(SESSION_VERIFIED_KEY + username);
        Long expiryTime = (Long) session.getAttribute(SESSION_EXPIRY_KEY + username);

        if (isVerified == null || !isVerified) {
            log.info("User {} trying to access password reset without email verification", username);
            return false;
        }

        if (expiryTime == null) {
            log.warn("Invalid session for user {} - missing expiry time", username);
            cleanupPasswordResetSession(username, session);
            return false;
        }

        // 5. Check session expiry
        long currentTime = System.currentTimeMillis();
        if (currentTime >= expiryTime) {
            cleanupPasswordResetSession(username, session);
            log.info("Password reset session expired for user: {}", username);
            return false;
        }

        // All checks passed
        log.debug("Password reset validation successful for user: {}", username);
        return true;
    }

    @Override
    public String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***@***.***";
        }

        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***@***.***";
        }

        String localPart = parts[0];
        String domainPart = parts[1];

        // Mask local part
        String maskedLocal;
        if (localPart.length() <= 3) {
            maskedLocal = localPart.charAt(0) + "***";
        } else {
            maskedLocal = localPart.substring(0, 2) + "***" + localPart.charAt(localPart.length() - 1);
        }

        // Mask domain part
        String[] domainParts = domainPart.split("\\.");
        String maskedDomain;
        if (domainParts.length > 1) {
            String tld = domainParts[domainParts.length - 1];
            maskedDomain = "***." + tld;
        } else {
            maskedDomain = "***";
        }

        return maskedLocal + "@" + maskedDomain;
    }

    @Override
    public boolean isPasswordResetSessionValid(String username, HttpSession session) {
        return canAccessPasswordResetPage(username, session);
    }

    @Override
    public long getRemainingTimeMs(String username, HttpSession session) {
        Long expiryTime = (Long) session.getAttribute(SESSION_EXPIRY_KEY + username);

        if (expiryTime == null) {
            return -1;
        }

        long remainingMs = expiryTime - System.currentTimeMillis();
        return Math.max(0, remainingMs);
    }

    @Override
    public void cleanupPasswordResetSession(String username, HttpSession session) {
        session.removeAttribute(SESSION_VERIFIED_KEY + username);
        session.removeAttribute(SESSION_EXPIRY_KEY + username);
        log.debug("Cleaned up password reset session for user: {}", username);
    }

    @Override
    public void processPasswordReset(PostForgetPasswordFinalRequest request, HttpSession session) {
        //1 Validate session is still valid
        if (!isPasswordResetSessionValid(request.getUsername(), session)) {

            throw new PasswordResetSessionExpiredException("Session expired. Please verify your email again.");
        }

        //2 Validate password length
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {

            throw new InvalidPasswordException("New password must be at least 6 characters long.");
        }

        //3 Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {

            throw new PasswordMismatchException("Passwords do not match. Please check and try again.");
        }

        //4 Validate password strength (optional)
        if (request.getNewPassword().equals(request.getUsername())) {

            throw new InvalidPasswordException("Password cannot be the same as username.");
        }

        try {
            //5 Update password
            userService.processPasswordChangeAndInvalidateSessions(request.getUsername(), request.getNewPassword());

            //6 Clean up session
            cleanupPasswordResetSession(request.getUsername(), session);

            //7 Log success
            log.info("Password reset completed successfully for user: {}", request.getUsername());

        } catch (Exception e) {
            log.error("Failed to process password reset for user: {}", request.getUsername(), e);
            throw new PasswordResetProcessException("Failed to update password. Please try again later.");
        }
    }
}
