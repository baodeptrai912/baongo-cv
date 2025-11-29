package com.example.baoNgoCv.exception.emailException;

import jakarta.mail.MessagingException;

public class EmailSendingException extends RuntimeException {
    private  String username;

    public EmailSendingException(String message, String username) {
        super(message);
        this.username = username;
    }

    public EmailSendingException(String message, Throwable cause, String username) {
        super(message, cause);
        this.username = username;
    }

    public EmailSendingException(String message, MessagingException e) {
    }


    public String getUsername() {
        return username;
    }
    }

