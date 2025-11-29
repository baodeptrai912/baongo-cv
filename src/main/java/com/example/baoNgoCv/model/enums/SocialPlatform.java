package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum định nghĩa các nền tảng mạng xã hội được hỗ trợ.
 * Mỗi nền tảng chứa tên hiển thị và lớp CSS icon tương ứng,
 * giúp giảm sự dư thừa dữ liệu trong bảng.
 */
@Getter
@RequiredArgsConstructor
public enum SocialPlatform {
    LINKEDIN("LinkedIn", "fab fa-linkedin-in"),
    GITHUB("GitHub", "fab fa-github"),
    PORTFOLIO("Portfolio", "fas fa-globe"),
    TWITTER("Twitter", "fab fa-twitter"),
    FACEBOOK("Facebook", "fab fa-facebook-f");

    /**
     * Tên hiển thị thân thiện với người dùng của nền tảng.
     */
    private final String displayName;

    /**
     * Lớp CSS của icon FontAwesome tương ứng với nền tảng.
     */
    private final String iconClass;
}