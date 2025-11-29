package com.example.baoNgoCv.model.dto.company;

/**
 * DTO response cho quá trình khởi tạo thay đổi mật khẩu.
 * Chứa thông tin trả về cho client một cách tường minh và an toàn kiểu.
 *
 * @param success Trạng thái thành công của hoạt động.
 * @param message Thông báo cho người dùng.
 * @param email   Email mà mã xác thực đã được gửi đến.
 */
public record PasswordChangeInitResponse(
        boolean success,
        String message,
        String email
) {
}
