package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.jpa.projection.user.BasicPersonalInfoDTO;

public record PostPersonalInfoResponse (
    String message,
    BasicPersonalInfoDTO personalInfo,
    String profileImageUrl
) {
        public static PostPersonalInfoResponse success(String message, BasicPersonalInfoDTO personalInfo, String profileImageUrl) {
            return new PostPersonalInfoResponse(message, personalInfo, profileImageUrl);
        }
    }
