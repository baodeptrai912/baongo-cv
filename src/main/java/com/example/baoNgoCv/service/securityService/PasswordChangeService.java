package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.model.dto.common.PasswordChangeRequest;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.session.PendingPasswordChange;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

public interface PasswordChangeService {

    void initiatePasswordChange(User currentUser, PasswordChangeRequest request) throws MessagingException;

    /**
     * Verifies the provided code and updates the user's password if the code is valid and not expired.
     *
     * @param currentUser The currently authenticated user.
     * @param verificationCode The verification code submitted by the user.
     */
    void verifyAndFinalizePasswordChange(User currentUser, String verificationCode);

    void resendPasswordChangeCode(User currentUser) throws MessagingException;

    @Transactional
    void deleteUser(User user);
}
