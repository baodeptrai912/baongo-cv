package com.example.baoNgoCv.model.dto.user;

import lombok.Data;

@Data
public class PostVerifyPasswordForDeletionResponse {

    private final String email;
    private final long expirationTimestamp;
    private final long remainingTime;

    public PostVerifyPasswordForDeletionResponse(String email, long expirationTimestamp) {
        this.email = email;
        this.expirationTimestamp = expirationTimestamp;
        this.remainingTime = Math.max(0, (expirationTimestamp - System.currentTimeMillis()) / 1000);
    }

}