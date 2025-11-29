package com.example.baoNgoCv.exception;

import com.example.baoNgoCv.exception.application.ApplicationNotFoundException;
import com.example.baoNgoCv.exception.companyException.PaymentNotFoundException;
import com.example.baoNgoCv.exception.companyException.UpgradePlanException;
import com.example.baoNgoCv.exception.jobpostingException.*;
import com.example.baoNgoCv.model.dto.common.ApiResponse;
import com.example.baoNgoCv.exception.registrationException.DuplicateRegistrationDataException;
import com.example.baoNgoCv.exception.emailException.EmailSendingException;
import com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException;
import com.example.baoNgoCv.exception.registrationException.RegistrationEmailMismatchException;
import com.example.baoNgoCv.exception.registrationException.RegistrationSessionExpiredException;
import com.example.baoNgoCv.exception.securityException.InvalidPasswordException;
import com.example.baoNgoCv.exception.securityException.PasswordMismatchException;
import com.example.baoNgoCv.exception.securityException.PasswordResetProcessException;
import com.example.baoNgoCv.exception.securityException.PasswordResetSessionExpiredException;
import com.example.baoNgoCv.exception.utilityException.FileUploadException;
import com.example.baoNgoCv.exception.jobseekerException.UserNotFoundException;
import com.example.baoNgoCv.exception.securityException.RateLimitExceededException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.thymeleaf.exceptions.TemplateInputException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(JobPostingLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobPostingLimitExceeded(JobPostingLimitExceededException ex) {
        log.warn("Job posting limit exceeded: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error("JOB_POSTING_LIMIT_EXCEEDED", "You have reached the limit of jop posting this month , View our plan if you want to upgrade !");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UpgradePlanException.class)
    public ResponseEntity<ProblemDetail> handleUpgradePlanException(UpgradePlanException ex) {
        log.warn("Upgrade plan failed: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Field-level validation error occurred");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.validationError(
                "Please check your information and try again",
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Class-level validation error occurred: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            log.info("FieldName: '{}', Message: '{}'", fieldName, errorMessage);
            if (fieldName == null || fieldName.isEmpty()) {
                fieldErrors.put("", errorMessage);
            } else {
                fieldErrors.put(fieldName, errorMessage);
            }
        });

        ApiResponse<Void> response = ApiResponse.validationError(
                "Please check your information and try again",
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeExceptions(RuntimeException ex) {

        log.error("Runtime exception occurred: {}", ex.getMessage(), ex);

        ApiResponse<Void> errorResponse = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity.status(500).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericExceptions(Exception ex) {
        log.error("Unexpected exception occurred", ex);

        ApiResponse<Void> errorResponse = ApiResponse.error(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later."
        );

        return ResponseEntity.status(500).body(errorResponse);
    }

    @ExceptionHandler(TemplateInputException.class)
    public String handleTemplateError(TemplateInputException e, Model model, HttpServletRequest request) {

        log.error("Template error at {}: {}", request.getRequestURI(), e.getMessage());

        model.addAttribute("errorMessage", "Template rendering failed");
        model.addAttribute("originalUrl", request.getRequestURI());

        return "status/500";
    }

    @ExceptionHandler(JobNotFoundExceptionHtml.class)
    public String handleJobNotFoundException(JobNotFoundExceptionHtml ex, Model model, HttpServletRequest request) {
        log.warn("Job not found exception: {} at {}", ex.getMessage(), request.getRequestURI());

        model.addAttribute("errorTitle", "Job Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorIcon", "fas fa-briefcase");
        model.addAttribute("errorIconColor", "text-warning");
        model.addAttribute("originalUrl", request.getRequestURI());
        model.addAttribute("timestamp", LocalDateTime.now());

        return "status/job-not-found";
    }

    @ExceptionHandler(JobNotFoundExceptionJson.class)
    public ResponseEntity<ApiResponse<Void>> handleJobNotFound(JobNotFoundExceptionJson ex) {
        log.warn("Job not found exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("JOB_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationNotFound(ApplicationNotFoundException ex) {
        log.warn("Application (Applicant) not found: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "APPLICATION_NOT_FOUND",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(JobAlreadySavedException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobAlreadySaved(JobAlreadySavedException ex) {
        log.warn("Job already saved exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("JOB_ALREADY_SAVED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(JobNotSavedException.class)
    public ResponseEntity<ApiResponse<Void>> handleJobNotSaved(JobNotSavedException ex) {
        log.warn("Job not saved exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("JOB_NOT_SAVED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.warn("Payment not found exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error("PAYMENT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ===== SPRING DATA ACCESS EXCEPTIONS =====

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.error("DATA_INTEGRITY_ERROR",
                "Unable to complete this action due to data constraints. Please check your information and try again.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }


    private String makeValidationMessageFriendly(String originalMessage) {
        if (originalMessage == null) return "This field is invalid";

        String message = originalMessage.toLowerCase();

        if (message.contains("must not be null") || message.contains("must not be blank") || message.contains("must not be empty")) {
            return "This field is required";
        }
        if (message.contains("size must be between")) {
            return "Please enter between the specified number of characters";
        }
        if (message.contains("must be a valid email")) {
            return "Please enter a valid email address";
        }
        if (message.contains("must be greater than")) {
            return "This value must be greater than the minimum allowed";
        }
        if (message.contains("must be less than")) {
            return "This value must be less than the maximum allowed";
        }
        if (message.contains("must match")) {
            return "This field doesn't match the required format";
        }

        return originalMessage.substring(0, 1).toUpperCase() + originalMessage.substring(1);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found exception: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "USER_NOT_AUTHENTICATED",
                "User not authenticated. Please login to continue."
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles exceptions when a user is not found by their username.
     * This is often thrown during authentication or user lookup processes.
     * We return a 404 Not Found to indicate the resource (user) doesn't exist.
     *
     * @param ex The exception thrown.
     * @return A ResponseEntity with status 404 Not Found and a ProblemDetail body.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("UsernameNotFoundException caught: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Can not find your username!."
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    /**
     * Handles exceptions when an operation requires authentication but no credentials are found.
     * This typically happens when a secured endpoint is accessed without a valid JWT or session.
     *
     * @param ex The exception thrown by Spring Security.
     * @return A ResponseEntity with status 401 Unauthorized and a ProblemDetail body.
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication credentials not found for a secured resource: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required to access this resource. Please log in."
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUpload(FileUploadException ex) {
        log.error("File upload exception: {}", ex.getMessage(), ex);

        ApiResponse<Void> response = ApiResponse.error(
                "FILE_UPLOAD_ERROR",
                "File upload failed: " + ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailSendingException(EmailSendingException e) {
        // Log lỗi hệ thống
        log.error("Failed to send email to {}: {}", e.getUsername(), e.getMessage());

        // Tạo phản hồi lỗi chuẩn
        ApiResponse<Void> response = ApiResponse.error(
                "EMAIL_SENDING_FAILED", // Mã lỗi
                "Failed to send email. Please check the address or try again later."
        );

        // Trả về 500 Internal Server Error (vì đây thường là lỗi hệ thống SMTP)
        // Hoặc 400 Bad Request nếu bạn chắc chắn do input người dùng sai.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(problemDetail);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidVerificationCode(InvalidVerificationCodeException ex) {
        log.warn("Invalid verification code attempt: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(PasswordResetSessionExpiredException.class)
    public ResponseEntity<ProblemDetail> handlePasswordResetSessionExpired(PasswordResetSessionExpiredException ex) {

        // Tự động tạo status 401 UNAUTHORIZED và set detail
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("Invalid password attempt: {}", ex.getMessage());

        // Tạo field errors map để frontend hiển thị lỗi ở đúng input
        Map<String, String> fieldErrors = Map.of(
                "currentPassword", ex.getMessage()
        );

        // Trả về ApiResponse thay vì ProblemDetail
        ApiResponse<Void> response = ApiResponse.validationError(
                ex.getMessage(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ProblemDetail> handlePasswordMismatch(PasswordMismatchException ex) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(problemDetail);
    }

    @ExceptionHandler(PasswordResetProcessException.class)
    public ResponseEntity<ProblemDetail> handlePasswordResetProcessError(PasswordResetProcessException ex) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );

        return ResponseEntity.internalServerError().body(problemDetail);
    }

    // ===== REGISTRATION EXCEPTIONS =====

    @ExceptionHandler(RegistrationSessionExpiredException.class)
    public ResponseEntity<ProblemDetail> handleRegistrationSessionExpired(RegistrationSessionExpiredException ex) {
        log.warn("Registration session expired: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(RegistrationEmailMismatchException.class)
    public ResponseEntity<ProblemDetail> handleRegistrationEmailMismatch(RegistrationEmailMismatchException ex) {
        log.warn("Registration email mismatch: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(DuplicateRegistrationDataException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateRegistrationData(DuplicateRegistrationDataException ex) {
        log.warn("Registration failed due to duplicate data: {}", ex.getErrors());

        return ResponseEntity
                .status(HttpStatus.CONFLICT) // HTTP 409
                .body(ApiResponse.errorWithDetails(
                        "CONFLICT",
                        "Username or Email already exists.",
                        ex.getErrors()
                ));
    }


    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex) {
        log.info("Client aborted the connection: {}", ex.getMessage());
    }


}
