package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException;
import com.example.baoNgoCv.exception.jobseekerException.UserNotFoundException;
import com.example.baoNgoCv.exception.securityException.InvalidPasswordException;
import com.example.baoNgoCv.exception.securityException.RateLimitExceededException;
import com.example.baoNgoCv.jpa.repository.UserRepository;
import com.example.baoNgoCv.model.dto.common.PasswordChangeRequest;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.VerificationType;
import com.example.baoNgoCv.model.session.PendingPasswordChange;
import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.utilityService.EmailService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordChangeServiceImpl implements PasswordChangeService {

    @Value("${app.verification.rate.limit.seconds}")
    private long rateLimitSeconds;

    @Value("${app.verification.code.expiry.seconds}")
    private long codeExpirySeconds;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PendingPasswordChange pendingPasswordChange;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initiatePasswordChange(User currentUser, PasswordChangeRequest request) throws MessagingException {
        // 1. Kiểm tra rate limit dựa trên session
        long waitTime = pendingPasswordChange.getWaitTime(rateLimitSeconds, codeExpirySeconds);
        if (waitTime > 0) {
            String message = "Please wait " + waitTime + " seconds before requesting another code.";
            throw new RateLimitExceededException(message, waitTime);
        }

        // 2. Xác thực mật khẩu hiện tại
        if (!userService.checkPassword(currentUser, request.currentPassword())) {
            throw new InvalidPasswordException("Incorrect current password.");
        }

        // 3. Mã hóa mật khẩu mới
        String newPasswordHashed = passwordEncoder.encode(request.newPassword());
        String userEmail = currentUser.getContactInfo().getEmail();

        // 4. Lưu email và mật khẩu đã mã hóa vào session bean
        pendingPasswordChange.initialize(userEmail, newPasswordHashed, codeExpirySeconds);

        // 5. Ủy quyền cho EmailService gửi mã xác thực
        emailService.sendVerification(userEmail, VerificationType.PASSWORD_CHANGE);
    }

    @Override
    @Transactional
    public void verifyAndFinalizePasswordChange(User currentUser, String verificationCode) {
        // 1. Kiểm tra trạng thái session
        if (pendingPasswordChange.isExpired() || pendingPasswordChange.getUserEmail() == null) {
            pendingPasswordChange.clear();
            throw new InvalidVerificationCodeException("Your password change session has expired. Please start over.");
        }

        // 2. Ủy quyền cho EmailService xác thực mã
        emailService.verifyCode(pendingPasswordChange.getUserEmail(), verificationCode);

        // 3. Nếu xác thực thành công, cập nhật mật khẩu
        String newPasswordHashed = pendingPasswordChange.getNewPasswordHashed();
        User userToUpdate = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException());
        userToUpdate.setPassword(newPasswordHashed);

        // 4. Dọn dẹp session sau khi hoàn tất
        pendingPasswordChange.clear();
    }

    @Override
    public void resendPasswordChangeCode(User currentUser) throws MessagingException {
        // 1. Kiểm tra xem có session đổi mật khẩu đang chờ không
        if (pendingPasswordChange.isExpired() || pendingPasswordChange.getUserEmail() == null) {
            throw new IllegalStateException("Your password change session has expired. Please start the password change process again.");
        }

        // 2. Kiểm tra rate limit
        long waitTime = pendingPasswordChange.getWaitTime(rateLimitSeconds, codeExpirySeconds);
        if (waitTime > 0) {
            throw new RateLimitExceededException("Please wait " + waitTime + " seconds before requesting another code.", waitTime);
        }

        // 3. Cập nhật lại thời gian hết hạn của session
        pendingPasswordChange.resetExpiryTime(codeExpirySeconds);

        // 4. Gửi lại email xác thực
        emailService.sendVerification(
                pendingPasswordChange.getUserEmail(),
                VerificationType.PASSWORD_CHANGE
        );
    }

    @Transactional
    @Override
    public void deleteUser(User user) {
        // 1. Clear collection trước (optional, nhưng safe)
        user.getFollowedCompanies().clear();
        userRepository.flush();

        // 3. Xóa user
        userRepository.delete(user);
    }
}
