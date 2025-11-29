package com.example.baoNgoCv.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for forget password final page data
 * Contains all necessary information for password reset form display
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetForgetPasswordFinalResponse {

    /**
     * Username for the password reset form (hidden field)
     */
    private String username;

    /**
     * Remaining time in minutes for session expiry
     */
    private long remainingMinutes;

    /**
     * Total remaining seconds for JavaScript countdown
     */
    private long totalRemainingSeconds;

    /**
     * Masked email for security display
     */
    private String maskedEmail;

    /**
     * Success message to display to user
     */
    private String message;

    /**
     * Whether session is expiring soon (less than 2 minutes)
     */
    private boolean expiringSoon;

    /**
     * Session expiry timestamp for frontend handling
     */
    private long expiryTimestamp;

    /**
     * Creates response with full data
     */
    public static GetForgetPasswordFinalResponse full(String username, long remainingTimeMs, String maskedEmail) {
        long remainingMinutes = remainingTimeMs / (60 * 1000);
        long totalSeconds = remainingTimeMs / 1000;
        long expiryTimestamp = System.currentTimeMillis() + remainingTimeMs;

        return GetForgetPasswordFinalResponse.builder()
                .username(username)
                .remainingMinutes(remainingMinutes)
                .totalRemainingSeconds(totalSeconds)
                .maskedEmail(maskedEmail)
                .expiringSoon(remainingMinutes < 2)
                .expiryTimestamp(expiryTimestamp)
                .message(remainingMinutes < 2 ?
                        "⚠️ Session expiring soon! Please reset your password quickly." :
                        "Email verified successfully! Please enter your new password below.")
                .build();
    }

    /**
     * Gets remaining seconds within current minute
     */
    public long getRemainingSeconds() {
        return totalRemainingSeconds % 60;
    }

    /**
     * Formats remaining time as MM:SS
     */
    public String getFormattedRemainingTime() {
        long minutes = remainingMinutes;
        long seconds = getRemainingSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }
}
