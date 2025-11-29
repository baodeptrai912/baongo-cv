package com.example.baoNgoCv.model.dto.jobposting;

import com.example.baoNgoCv.model.entity.JobPosting;
import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class PostJobResponse {
    private Long id;

    public static PostJobResponse from(JobPosting jobPosting) {
        return PostJobResponse.builder()
                .id(jobPosting.getId())
                .build();
    }

}
