package com.example.baoNgoCv.model.dto.applicant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportResult {
    private String fileName;
    private byte[] data;
    private String contentType;
}
