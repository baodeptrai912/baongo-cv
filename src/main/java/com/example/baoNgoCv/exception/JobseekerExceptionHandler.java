package com.example.baoNgoCv.exception;

import com.example.baoNgoCv.controller.JobseekerController;
import com.example.baoNgoCv.model.dto.common.ApiResponse;
import com.example.baoNgoCv.exception.application.ApplicationLimitReachedException;
import com.example.baoNgoCv.exception.application.DuplicateApplicationException;
import com.example.baoNgoCv.exception.educationException.EducationOverlapException;
import com.example.baoNgoCv.exception.application.ApplicantStatusTransitionException;
import com.example.baoNgoCv.exception.educationException.InvalidEducationDateException;
import com.example.baoNgoCv.exception.jobExperienceException.InvalidJobDateException;
import com.example.baoNgoCv.exception.jobExperienceException.JobExperienceOverlapException;
import com.example.baoNgoCv.exception.educationException.EducationNotFoundException;
import com.example.baoNgoCv.exception.jobpostingException.JobNotFoundExceptionJson;
import com.example.baoNgoCv.exception.jobseekerException.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {JobseekerController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class JobseekerExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundRedirect(UserNotFoundException ex) {
        log.error("User not found, redirecting to login: {}", ex.getMessage(), ex);
        return "redirect:/main/home";
    }

    @ExceptionHandler(EducationNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEducationNotFound(EducationNotFoundException ex) {
        log.warn("Education not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("EDUCATION_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(EducationOverlapException.class)
    public ResponseEntity<ApiResponse<Object>> handleEducationOverlap(EducationOverlapException ex) {
        log.warn("Education period overlap detected: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("EDUCATION_OVERLAP", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidEducationDateException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidEducationDate(InvalidEducationDateException ex) {
        log.warn("Invalid education date detected: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("INVALID_EDUCATION_DATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(JobExperienceOverlapException.class)
    public ResponseEntity<ApiResponse<Object>> handleJobExperienceOverlap(JobExperienceOverlapException ex) {
        log.warn("Job experience period overlap detected: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("JOB_EXPERIENCE_OVERLAP", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidJobDateException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidJobDate(InvalidJobDateException ex) {
        log.warn("Invalid job date detected: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("INVALID_JOB_DATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("ACCESS_DENIED", "You are not alowed to do this action");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(JobExperienceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobExperienceNotFound(JobExperienceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(JobNotFoundExceptionJson.class)
    public ResponseEntity<ApiResponse<Object>> handleJobNotFound(JobNotFoundExceptionJson ex) {
        log.warn("Job posting not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error("JOB_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(JobExpiredException.class)
    public ResponseEntity<ApiResponse<Object>> handleJobExpired(JobExpiredException ex) {
        log.warn("Job application failed - job expired: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "JOB_EXPIRED",
                "This job posting has expired and is no longer accepting applications"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ProfileIncompleteException.class)
    public ResponseEntity<ApiResponse<Object>> handleProfileIncomplete(ProfileIncompleteException ex) {
        log.warn("Job application failed - incomplete profile: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.errorWithDetails(
                "PROFILE_INCOMPLETE",
                "Please complete your profile before applying for jobs",
                "Redirect user to profile completion page"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateApplication(DuplicateApplicationException ex) {
        log.warn("Duplicate job application attempt: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "DUPLICATE_APPLICATION",
                "You have already applied for this job position"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ApplicationLimitReachedException.class)
    public ResponseEntity<ApiResponse<Object>> handleApplicationLimitReached(ApplicationLimitReachedException ex) {
        log.warn("Job application failed - limit reached: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "APPLICATION_LIMIT_REACHED",
                "This job posting has reached the maximum number of applications"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApplicantStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Object>> handleApplicantStatusTransition(ApplicantStatusTransitionException ex) {
        log.warn("Invalid application status transition: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "INVALID_STATUS_TRANSITION",
                ex.getMessage() // Trả về chính xác thông báo lỗi từ service
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadError(FileUploadException ex) {
        log.error("CV file upload failed: {}", ex.getMessage(), ex);

        ApiResponse<Object> response = ApiResponse.errorWithDetails(
                "FILE_UPLOAD_ERROR",
                "Failed to upload CV file. Please try again",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileSizeExceeded(FileSizeLimitExceededException ex) {
        log.warn("CV file size exceeded: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.errorWithDetails(
                "FILE_SIZE_EXCEEDED",
                "CV file size is too large. Maximum allowed size is 5MB",
                "Maximum size: 5MB"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(UserEmailNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserEmailNotFound(UserEmailNotFoundException ex) {
        log.warn("User email not configured: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(InvalidSocialLinksException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSocialLinksException(
            InvalidSocialLinksException ex) {

        log.warn("Invalid social links: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Social links must not be duplicate!"
        );



        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

}
