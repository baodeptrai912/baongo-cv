package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicantFilterRequest {
    private Long jobId;
    private ApplicationStatus status;
}