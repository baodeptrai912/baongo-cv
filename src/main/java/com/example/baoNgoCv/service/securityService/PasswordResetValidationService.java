package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.dto.user.GetForgetPasswordFinalResponse;
import com.example.baoNgoCv.model.dto.user.PostForgetPasswordFinalRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Service for validating password reset operations and email masking utilities
 */
public interface PasswordResetValidationService {

    /**
     * Creates page data for forget password final page
     *
     * @param username The username requesting password reset
     * @return GetForgetPasswordFinalResponse with page data
     */
    GetForgetPasswordFinalResponse createPasswordResetPageData(String username,long re);

    /**
     * Masks email address for privacy display
     *
     * @param email The email address to mask
     * @return Masked email string (e.g., "jo***@g***.com")
     */
    String maskEmail(String email);

    /**
     * Checks if password reset session is still valid for given username
     *
     * @param username The username to check
     * @param session HttpSession to validate
     * @return true if session is valid and not expired
     */
    boolean isPasswordResetSessionValid(String username, HttpSession session);

    /**
     * Gets remaining time in milliseconds for password reset session
     *
     * @param username The username to check
     * @param session HttpSession containing expiry time
     * @return Remaining time in milliseconds, -1 if session invalid
     */
    long getRemainingTimeMs(String username, HttpSession session);

    /**
     * Cleans up expired password reset sessions for given username
     *
     * @param username The username to clean up
     * @param session HttpSession to clean
     */
    void cleanupPasswordResetSession(String username, HttpSession session);

    /**
     * Processes password reset with validation
     * @param request Password reset request
     * @param session HTTP session
     */
    void processPasswordReset(PostForgetPasswordFinalRequest request, HttpSession session);
}
