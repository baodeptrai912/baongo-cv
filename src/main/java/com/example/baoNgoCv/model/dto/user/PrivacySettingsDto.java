package com.example.baoNgoCv.model.dto.user;

import lombok.Data;

/**
 * DTO for updating user's privacy settings.
 * Used in the request body for the privacy update endpoint.
 */
@Data
public class PrivacySettingsDto {
    private boolean profilePublic;
}