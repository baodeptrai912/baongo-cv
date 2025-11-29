package com.example.baoNgoCv.service.securityService;

import org.springframework.security.core.Authentication;

public interface NotificationSecurityService {
    boolean isOwner(Long notificationId);
}
