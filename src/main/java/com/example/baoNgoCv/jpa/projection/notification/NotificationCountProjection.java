package com.example.baoNgoCv.jpa.projection.notification;

public interface NotificationCountProjection {
    long getAllCount();
    long getUnreadCount();
    long getReadCount();
}