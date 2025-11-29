package com.example.baoNgoCv.jpa.projection.notification;

import com.example.baoNgoCv.model.enums.NotificationType;

import java.time.LocalDateTime;

public interface NotificationProjection {
    // Các trường lấy trực tiếp từ bảng Notification
    Long getId();
    String getTitle();
    String getMessage();
    String getAvatar();
    String getHref();
    NotificationType getType();
    boolean getIsRead();
    LocalDateTime getCreatedAt();

    // --- TRƯỜNG ALIAS (Được tính toán trong câu Query) ---

    /**
     * Tên người gửi (User hoặc Company) đã được xử lý bằng CASE WHEN hoặc COALESCE trong SQL
     * Alias trong query sẽ là: "senderName"
     */
    String getSenderName();
}
