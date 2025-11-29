package com.example.baoNgoCv.model.dto;

import com.example.baoNgoCv.model.enums.AlertFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAlertResponse {
    private Long id;
    private String alertName;
    private String keyword;
    private String location;
    private String experience;
    private String salaryRange;
    private AlertFrequency frequency;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
    private String status;
}