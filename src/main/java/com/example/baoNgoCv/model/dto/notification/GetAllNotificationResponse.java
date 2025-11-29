package com.example.baoNgoCv.model.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi cho API /notification/filtering
 * Cấu trúc lồng nhau để khớp với JSON mà JS mong đợi.
 */
public record GetAllNotificationResponse(
        boolean success,
        NotificationPage notifications,
        NotificationCounts counts
) {

    /**
     * Record đại diện cho phân trang (Page Interface của Spring Data)
     * JS dùng: data.notifications.content, totalPages, number, ...
     */
    public record NotificationPage(
            List<NotificationItem> content,
            int totalPages,
            long totalElements,
            int number, // Current page index
            boolean first,
            boolean last
    ) {}

    /**
     * Record đại diện cho từng thông báo chi tiết
     * JS dùng: n.id, n.read, n.href, n.avatar, n.senderName, ...
     */
    public record NotificationItem(
            Long id,
            String title,
            String message,
            String avatar,
            String href,
            String type,

            @JsonProperty("read") // JS check if(!n.read)
            boolean isRead,

            String senderName,
            LocalDateTime createdAt // JS sẽ tự format bằng n.timeAgo
    ) {}

    /**
     * Record đại diện cho số lượng thống kê
     * JS dùng: data.counts.all, data.counts.unread, data.counts.read
     */
    public record NotificationCounts(
            long all,
            long unread,
            long read
    ) {}
}
