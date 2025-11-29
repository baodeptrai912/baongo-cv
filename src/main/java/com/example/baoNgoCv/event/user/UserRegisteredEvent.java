package com.example.baoNgoCv.event.user;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new user has successfully completed registration and
 * their data has been committed to the database.
 * This event is intended to be handled by transactional event listeners
 * to perform post-registration actions like sending welcome notifications.
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final Long userId;
    private final String username;
    private final String email;
    public UserRegisteredEvent(Object source, Long userId, String username, String email) {
        super(source);
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}