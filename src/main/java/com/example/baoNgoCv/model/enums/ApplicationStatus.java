package com.example.baoNgoCv.model.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus {

    // 1. Mới nộp (Chưa ai sờ vào) -> Màu Xám/Vàng
    PENDING("Pending Review", "text-bg-secondary"),

    // [NEW] 2. Đang xem xét (HR đã click vào xem) -> Màu Vàng/Xanh dương nhạt
    REVIEWING("Under Review", "text-bg-info"),
    // Hoặc dùng "text-bg-warning" nếu muốn nhấn mạnh đang xử lý

    // 3. Đã chọn vào vòng sau (Kết quả tích cực) -> Màu Xanh dương đậm
    SHORTLISTED("Shortlisted", "text-bg-primary"),

    // 4. Đã chốt lịch -> Màu Xanh lá
    INTERVIEW_SCHEDULED("Interview Scheduled", "text-bg-success"),

    // 5. Từ chối -> Màu Đỏ
    REJECTED("Rejected", "text-bg-danger"),

    // 6. Tự rút lui -> Màu Đen/Xám đậm
    WITHDRAWN("Withdrawn", "text-bg-dark");

    private final String label;
    private final String colorClass;

    ApplicationStatus(String label, String colorClass) {
        this.label = label;
        this.colorClass = colorClass;
    }
}
