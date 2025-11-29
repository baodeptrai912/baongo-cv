package com.example.baoNgoCv.model.dto.applicant;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ExportApplicationsRequest {
    private Long userId;
    private String format;
    private String status;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}