package com.example.baoNgoCv.event.applicant;

/**
 * Sự kiện được bắn ra khi một ứng viên nộp đơn ứng tuyển thành công.
 * Chứa các thông tin cần thiết để các listener (ví dụ: NotificationService) xử lý.
 */
public record ApplicationSubmittedEvent(
        /** ID của đơn ứng tuyển (Applicant) vừa được tạo. */
        Long applicantId,
        Long companyId,

        /** username của công ty. */
        String companyUsername,

        /** Tên đầy đủ của ứng viên nộp đơn. */
        String candidateFullName,

        /** URL ảnh đại diện của ứng viên. */
        String candidateAvatarUrl,

        /** Chức danh của công việc đã được ứng tuyển. */
        String jobTitle,

        /** Email của nhà tuyển dụng (để gửi thông báo). */
        String employerEmail,

        /** Tên của công ty tuyển dụng. */
        String employerName,

        /** Cài đặt của nhà tuyển dụng có cho phép nhận email thông báo không. */
        Boolean emailNotificationEnabled) {
}
