package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.enums.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;/**
 * A Data Transfer Object representing a single social link for update requests.
 * This is an immutable record, ensuring data integrity.
 *
 * @param platform The social media platform. Must not be null.
 * @param url      The full URL to the user's profile. Must be a valid, non-blank URL.
 */
public record PostSocialLinkRequest(
        @NotNull(message = "Platform cannot be null.")
        SocialPlatform platform,

        @NotBlank(message = "URL cannot be blank.")
        @URL(message = "Must be a valid URL format (e.g., https://...).")
        @Size(max = 512, message = "URL cannot exceed 512 characters.")
        String url
) {
}