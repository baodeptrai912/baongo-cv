package com.example.baoNgoCv.model.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private T data;
    private String error;
    private Object details;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String error, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> errorWithDetails(String error, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> validationError(String message, Object fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error("VALIDATION_ERROR")
                .message(message)
                .details(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> successWithDetails(T data, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }


}
