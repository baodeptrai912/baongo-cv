package com.example.baoNgoCv.event.user;

import java.util.List;

/**
 * Event published after a user account has been successfully deleted from database.
 * Contains minimal data needed for post-deletion cleanup tasks.
 */
public record UserAccountDeletedEvent(
        String email,
        List<String> filePaths
) {
}