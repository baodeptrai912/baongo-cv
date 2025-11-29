package com.example.baoNgoCv.model.dto.homepage;

import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
@Builder
@Data
@ToString
public class GetHomePageResponse {
    private List<JobCardDTO> jobCards;
    private Integer totalJobs;
    private Integer featuredJobs;
}
