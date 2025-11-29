package com.example.baoNgoCv.model.dto;

public class NotificationSettingsRequestDTO {
    private boolean emailNewApplicant;

    // Getter and Setter
    public boolean isEmailNewApplicant() {
        return emailNewApplicant;
    }

    public void setEmailNewApplicant(boolean emailNewApplicant) {
        this.emailNewApplicant = emailNewApplicant;
    }
}