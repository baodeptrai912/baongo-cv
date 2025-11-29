package com.example.baoNgoCv.model.dto.common;

/**
 * An interface representing a contract for any DTO that needs
 * to validate matching passwords.
 */
public interface PasswordConfirmation {

    String getPassword();

    String getConfirmPassword();
}