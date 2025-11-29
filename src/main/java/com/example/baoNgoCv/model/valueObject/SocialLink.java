package com.example.baoNgoCv.model.valueObject;

import com.example.baoNgoCv.model.enums.SocialPlatform;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialLink {

    /**
     * Nền tảng mạng xã hội (ví dụ: LINKEDIN, GITHUB).
     * Được lưu dưới dạng String trong DB để dễ đọc.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 50)
    private SocialPlatform platform;

    /**
     * Đường dẫn URL đầy đủ đến trang cá nhân trên nền tảng.
     */
    @Column(name = "url", nullable = false, length = 512)
    private String url;
}