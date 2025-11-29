package com.example.baoNgoCv.model.dto.company;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostBulkScheduleInterviewRequest {

    @NotEmpty(message = "Applicant IDs cannot be empty.")
    private List<Long> applicantIds;

    @NotEmpty(message = "Email subject cannot be empty.")
    private String subject;

    @NotNull(message = "Email content cannot be null.")
    private String content;
}