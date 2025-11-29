package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.dto.user.PostRegisterRequest;
import com.example.baoNgoCv.exception.registrationException.EmailAlreadyExistsException;
import com.example.baoNgoCv.exception.registrationException.UsernameAlreadyExistsException;
import com.example.baoNgoCv.exception.securityException.PasswordMismatchException;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.VerificationType;
import com.example.baoNgoCv.service.domainService.CompanyService;
import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.model.session.PendingUserRegistration;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final UserService userService;
    private final CompanyService companyService;
    private final EmailService emailService;
    private final PendingUserRegistration pendingUserRegistration;

    @Override
    public void processRegistration(PostRegisterRequest request) throws MessagingException {
        // 1. Validate username
        User existingUser = userService.findByUsername(request.getUsername());
        Optional<Company> existingCompany = companyService.findByUserName(request.getUsername());
        if (existingUser != null || existingCompany.isPresent()) {
            throw new UsernameAlreadyExistsException("Username is already taken, please choose another.");
        }

        // 2. Validate email
        Optional<User> existingEmail = userService.findByEmail(request.getEmail());
        if (existingEmail.isPresent()) {
            throw new EmailAlreadyExistsException("Email is already taken, please choose another.");
        }

        // 3. Validate password match
        if (!request.getConfirmPassword().equals(request.getPassword())) {
            throw new PasswordMismatchException("The password confirmation does not match.");
        }

        // 4. Send verification email using the unified method
        emailService.sendVerification(request.getEmail(), VerificationType.REGISTRATION);

        // 5. Store pending data in session
        pendingUserRegistration.storeRegistrationData(request);
        log.info("Verification code sent to {}. Registration data stored in session bean.", request.getEmail());
    }
}
