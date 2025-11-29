package com.example.baoNgoCv.exception;

import com.example.baoNgoCv.controller.CompanyController;
import com.example.baoNgoCv.exception.application.ApplicantStatusTransitionException;
import com.example.baoNgoCv.exception.application.BulkApplicantActionException;
import com.example.baoNgoCv.exception.application.ReviewNotAllowedException;
import com.example.baoNgoCv.exception.jobpostingException.*;
import com.example.baoNgoCv.exception.jobpostingException.InvalidJobPostingDataException;
import com.example.baoNgoCv.exception.jobpostingException.JobPostingDatabaseException;
import com.example.baoNgoCv.exception.jobpostingException.JobPostingLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.example.baoNgoCv.model.dto.common.ApiResponse;
import com.example.baoNgoCv.exception.companyException.InvalidPasswordChangeException;
import com.example.baoNgoCv.exception.companyException.CompanyNotFoundException;
import com.example.baoNgoCv.exception.utilityException.FileUploadException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice(assignableTypes = {CompanyController.class})
@Slf4j
public class CompanyExceptionHandler {

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleCompanyNotFound(
            CompanyNotFoundException ex) {
        log.warn("Company not found: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("COMPANY_NOT_FOUND", "Cant find any company!"));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleFileUpload(
            FileUploadException ex) {
        log.warn("File upload error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FILE_UPLOAD_ERROR", "Lỗi tải file"));
    }

    @ExceptionHandler(DuplicateJobPostingException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateJobPosting(
            DuplicateJobPostingException ex) {
        log.warn("Duplicate job posting: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_JOB_POSTING", "Tin tuyển dụng đã tồn tại"));
    }

    @ExceptionHandler(InvalidJobPostingDataException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInvalidJobPostingData(
            InvalidJobPostingDataException ex) {
        log.warn("Invalid job posting data: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_JOB_DATA", "Dữ liệu tin tuyển dụng không hợp lệ"));
    }

    @ExceptionHandler(InvalidJobCreationDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJobCreationData(InvalidJobCreationDataException ex) {
        log.warn("Invalid job creation data: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_JOB_CREATION_DATA", ex.getMessage()));
    }

    @ExceptionHandler(JobPostingDatabaseException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleJobPostingDatabase(
            JobPostingDatabaseException ex) {
        log.error("Job posting database error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("JOB_POSTING_ERROR", "Không thể tạo tin tuyển dụng, vui lòng thử lại"));
    }

    @ExceptionHandler(JobPostingLimitExceededException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleJobPostingLimitExceeded(
            JobPostingLimitExceededException ex) {
        log.warn("Job posting limit exceeded: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("JOB_POSTING_LIMIT_EXCEEDED", ex.getMessage()));
    }

    @ExceptionHandler(JobPostingUpdateException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobPostingUpdateException(JobPostingUpdateException ex) {
        log.warn("Job posting update failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("UPDATE_FAILED", ex.getMessage()));
    }

    // ✅ Fallback DataAccess Exception Handlers (chỉ khi không được translate trong service)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDuplicateKey(
            DuplicateKeyException ex) {
        log.warn("Duplicate key violation: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_DATA", "Dữ liệu đã tồn tại"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_DATA", "Dữ liệu không hợp lệ"));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleDataAccessResource(
            DataAccessResourceFailureException ex) {
        log.error("Database connection failed: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("SERVICE_UNAVAILABLE", "Dịch vụ tạm thời không khả dụng"));
    }

    @ExceptionHandler(InvalidPasswordChangeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPasswordChange(InvalidPasswordChangeException ex) {
        log.warn("Invalid password change attempt: {}", ex.getErrors());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.validationError(
                        "Password update failed. Please check the details.",
                        ex.getErrors()));
    }

    @ExceptionHandler(ApplicantStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleStatusTransitionException(ApplicantStatusTransitionException ex) {
        // Ghi log cảnh báo vì đây là lỗi nghiệp vụ do client gửi data không hợp lệ
        log.warn("Applicant status transition failed: {}", ex.getMessage());

        // Tạo đối tượng phản hồi lỗi:
        // Code: INVALID_STATUS_TRANSITION
        // Message: Lấy từ message của Exception (ví dụ: "Cannot shortlist a rejected application.")
        ApiResponse<Void> response = ApiResponse.error(
                "INVALID_STATUS_TRANSITION",
                ex.getMessage()
        );

        // Trả về HTTP Status 400 Bad Request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ReviewNotAllowedException.class)
    public ResponseEntity<ApiResponse<Void>> handleReviewNotAllowedException(ReviewNotAllowedException ex) {
        // Ghi log cảnh báo vì đây là lỗi nghiệp vụ do client gửi data không hợp lệ
        log.warn("Review not allowed: {}", ex.getMessage());

        // Tạo đối tượng phản hồi lỗi
        ApiResponse<Void> response = ApiResponse.error(
                "REVIEW_NOT_ALLOWED",
                ex.getMessage()
        );

        // Trả về HTTP Status 400 Bad Request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BulkApplicantActionException.class)
    public ResponseEntity<ApiResponse<Void>> handleBulkActionException(BulkApplicantActionException ex) {
        // Ghi log lỗi với mức độ WARN vì đây là lỗi nghiệp vụ do client
        log.warn("Bulk action failed");

        // Tạo đối tượng phản hồi lỗi
        ApiResponse<Void> response = ApiResponse.error(
                "BULK_ACTION_FAILED",
                ex.getMessage() // Message đã được format rất rõ ràng từ service
        );

        // Trả về HTTP Status 400 Bad Request, cho biết rằng yêu cầu từ client không hợp lệ
        // và toàn bộ thao tác đã được rollback.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
