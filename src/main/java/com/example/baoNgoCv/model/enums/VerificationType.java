package com.example.baoNgoCv.model.enums;

import lombok.Getter;

@Getter
public enum VerificationType {
    REGISTRATION("BaoNgoCV - Email Verification Code", "emails/registration-verification"),
    PASSWORD_CHANGE("Your Password Change Verification Code", "emails/password-change"),
    ACCOUNT_DELETION("🚨 BaoNgoCV - Account Deletion Verification Code", "emails/delete-account-verification"),
    FORGET_PASSWORD("BaoNgoCV - Reset Your Password", "emails/forget-password");

    private final String subject;
    private final String templateName;

    VerificationType(String subject, String templateName) {
        this.subject = subject;
        this.templateName = templateName;
    }
}
