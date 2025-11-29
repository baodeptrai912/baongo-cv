package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.dto.user.PostRegisterRequest;
import jakarta.mail.MessagingException;

public interface RegistrationService {

    /**
     * Handles the initial user registration process: validation, and sending verification email.
     *
     * @param request The registration request DTO.
     * @throws com.example.baoNgoCv.exception.registrationException.UsernameAlreadyExistsException if username is taken.
     * @throws com.example.baoNgoCv.exception.registrationException.EmailAlreadyExistsException if email is taken.
     * @throws MessagingException if email sending fails.
     */
    void processRegistration(PostRegisterRequest request) throws MessagingException;
}