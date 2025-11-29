package com.example.baoNgoCv.model.session;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@Data
@SessionScope
public class PendingPasswordChange {

    private String userEmail; // Thêm trường này để lưu email của người dùng
    private String newPasswordHashed;
    private Long expiryTime; // Thời gian hết hạn của session, không phải của mã xác thực

    /**
     * Khởi tạo session đổi mật khẩu.
     * @param userEmail Email của người dùng đang đổi mật khẩu.
     * @param newPasswordHashed Mật khẩu mới đã được mã hóa.
     * @param validityDurationInSeconds Thời gian hiệu lực của session (không phải mã xác thực).
     */
    public void initialize(String userEmail, String newPasswordHashed, long validityDurationInSeconds) {
        this.userEmail = userEmail;
        this.newPasswordHashed = newPasswordHashed;
        this.expiryTime = System.currentTimeMillis() + (validityDurationInSeconds * 1000L);
    }

    /**
     * Cập nhật thời gian hết hạn của session (cho resend).
     * @param validityDurationInSeconds Thời gian hiệu lực mới của session.
     */
    public void resetExpiryTime(long validityDurationInSeconds) {
        this.expiryTime = System.currentTimeMillis() + (validityDurationInSeconds * 1000L);
    }

    /**
     * Tính toán thời gian chờ còn lại trước khi có thể yêu cầu mã mới.
     * @param rateLimitInSeconds Giới hạn tần suất gửi mã (giây).
     * @param sessionValidityInSeconds Thời gian hiệu lực của session (giây).
     * @return Thời gian chờ còn lại (giây), 0 nếu có thể gửi ngay.
     */
    public long getWaitTime(long rateLimitInSeconds, long sessionValidityInSeconds) {
        if (this.expiryTime == null) {
            return 0; // Chưa có session nào, có thể gửi ngay
        }

        // Thời điểm session được khởi tạo
        long creationTime = this.expiryTime - (sessionValidityInSeconds * 1000L);
        long timeSinceLastRequest = System.currentTimeMillis() - creationTime;
        long timeSinceLastRequestInSeconds = timeSinceLastRequest / 1000L;

        return Math.max(0, rateLimitInSeconds - timeSinceLastRequestInSeconds);
    }

    public boolean isExpired() {
        if (this.expiryTime == null) {
            return true;
        }
        return System.currentTimeMillis() > this.expiryTime;
    }

    public void clear() {
        this.userEmail = null;
        this.newPasswordHashed = null;
        this.expiryTime = null;
    }
}
