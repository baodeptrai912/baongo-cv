package com.example.baoNgoCv.exception.application;

import lombok.Getter;

/**
 * Exception dành riêng cho các lỗi xảy ra trong quá trình xử lý hàng loạt (bulk action) các ứng viên.
 * <p>
 * Được ném ra khi một hành động trên một ứng viên trong danh sách thất bại,
 * nhằm mục đích rollback toàn bộ transaction và cung cấp thông báo lỗi rõ ràng.
 */
@Getter
public class BulkApplicantActionException extends RuntimeException {

    public BulkApplicantActionException(String message) {
        super(message);

    }
}